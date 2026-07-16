// MapAi open-source backend
// Built with Express + better-sqlite3 + Socket.IO
// All libraries used here are open source (MIT / BSD).

import express from "express";
import http from "http";
import cors from "cors";
import multer from "multer";
import { Server } from "socket.io";
import Database from "better-sqlite3";
import path from "path";
import fs from "fs";

const PORT = process.env.PORT || 3000;
const DATA_DIR = process.env.DATA_DIR || "./data";
const UPLOAD_DIR = path.join(DATA_DIR, "uploads");
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const db = new Database(path.join(DATA_DIR, "mapai.db"));
db.pragma("journal_mode = WAL");

db.exec(`
  CREATE TABLE IF NOT EXISTS reports (
    id TEXT PRIMARY KEY,
    type TEXT,
    lat REAL,
    lon REAL,
    description TEXT,
    reporter TEXT,
    created_at INTEGER,
    confirmed INTEGER DEFAULT 0,
    media TEXT
  );

  CREATE TABLE IF NOT EXISTS chat (
    id TEXT PRIMARY KEY,
    role TEXT,
    content TEXT,
    created_at INTEGER
  );

  CREATE TABLE IF NOT EXISTS sos (
    id TEXT PRIMARY KEY,
    user TEXT,
    lat REAL,
    lon REAL,
    message TEXT,
    created_at INTEGER
  );

  CREATE TABLE IF NOT EXISTS media_feed (
    id TEXT PRIMARY KEY,
    author TEXT,
    filename TEXT,
    kind TEXT,
    lat REAL,
    lon REAL,
    caption TEXT,
    created_at INTEGER
  );

  CREATE TABLE IF NOT EXISTS nearby (
    id TEXT PRIMARY KEY,
    name TEXT,
    kind TEXT,
    lat REAL,
    lon REAL,
    meta TEXT,
    created_at INTEGER
  );

  CREATE TABLE IF NOT EXISTS contributors (
    id TEXT PRIMARY KEY,
    name TEXT,
    points INTEGER DEFAULT 0,
    reports INTEGER DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS cctv (
    id TEXT PRIMARY KEY,
    name TEXT,
    url TEXT,
    lat REAL,
    lon REAL
  );

  CREATE TABLE IF NOT EXISTS places (
    id TEXT PRIMARY KEY,
    name TEXT,
    category TEXT,
    lat REAL,
    lon REAL,
    rating REAL,
    is_open INTEGER DEFAULT 1,
    fuel_price TEXT,
    extra TEXT,
    created_at INTEGER
  );
`);

const app = express();
app.use(cors());
app.use(express.json({ limit: "10mb" }));

const upload = multer({ dest: UPLOAD_DIR, limits: { fileSize: 8 * 1024 * 1024 } });

const server = http.createServer(app);
const io = new Server(server, { cors: { origin: "*" } });

// ---- helpers ----
const uid = () => Math.random().toString(36).slice(2) + Date.now().toString(36);

// ---- Reports (crowd-sourced traffic / hazards) ----
app.get("/api/reports", (req, res) => {
  const { lat, lon, radius = 5 } = req.query;
  let rows = db.prepare("SELECT * FROM reports ORDER BY created_at DESC LIMIT 200").all();
  if (lat && lon) {
    const r = parseFloat(radius);
    rows = rows.filter(r2 => {
      const d = Math.hypot(r2.lat - parseFloat(lat), r2.lon - parseFloat(lon)) * 111;
      return d <= r;
    });
  }
  res.json(rows);
});

app.post("/api/reports", (req, res) => {
  const { type, lat, lon, description, reporter = "anon", media } = req.body;
  const id = uid();
  db.prepare(`INSERT INTO reports (id,type,lat,lon,description,reporter,created_at,confirmed,media)
    VALUES (?,?,?,?,?,?,?,?,?)`).run(id, type, lat, lon, description, reporter, Date.now(), 1, media || null);
  const row = db.prepare("SELECT * FROM reports WHERE id=?").get(id);
  io.emit("report:new", row);
  bumpContributor(reporter);
  res.json(row);
});

app.post("/api/reports/:id/confirm", (req, res) => {
  db.prepare("UPDATE reports SET confirmed = confirmed + 1 WHERE id=?").run(req.params.id);
  res.json({ ok: true });
});

// ---- Chat (proxy to a configurable LLM; defaults to a simple echo fallback) ----
app.post("/api/chat", async (req, res) => {
  const { messages } = req.body || {};
  const last = (messages || []).slice(-1)[0]?.content || "";
  // Open-source placeholder: echoes a helpful local response.
  // To use a real AI, set LLM_ENDPOINT / LLM_KEY env and implement fetch() here.
  const reply = aiFallback(last);
  const row = { id: uid(), role: "assistant", content: reply, created_at: Date.now() };
  db.prepare("INSERT INTO chat (id,role,content,created_at) VALUES (?,?,?,?)").run(row.id, row.role, row.content, row.created_at);
  res.json(row);
});

function aiFallback(text) {
  const t = (text || "").toLowerCase();
  if (t.includes("macet") || t.includes("traffic")) return "Cek laporan terdekat di tab Laporan. Hindari ruas utama saat jam sibuk.";
  if (t.includes("rute") || t.includes("route")) return "Tentukan tujuan di Peta, lalu ketuk 'Mulai Navigasi' untuk rute terbaik.";
  if (t.includes("sos") || t.includes("darurat")) return "Buka menu Darurat/SOS, ketuk tombol merah untuk kirim lokasi ke kontak darurat.";
  return "Saya asisten MapAi. Saya bisa bantu soal navigasi, lalu lintas, dan fitur darurat. Tanya apa saja!";
}

// ---- SOS ----
app.post("/api/sos", (req, res) => {
  const { user = "anon", lat, lon, message = "SOS" } = req.body;
  const id = uid();
  db.prepare("INSERT INTO sos (id,user,lat,lon,message,created_at) VALUES (?,?,?,?,?,?)").run(id, user, lat, lon, message, Date.now());
  io.emit("sos:new", { id, user, lat, lon, message });
  res.json({ ok: true, id });
});
app.get("/api/sos", (req, res) => res.json(db.prepare("SELECT * FROM sos ORDER BY created_at DESC LIMIT 50").all()));

// ---- Nearby (BLE / wifi / contributors broadcast) ----
app.get("/api/nearby", (req, res) => res.json(db.prepare("SELECT * FROM nearby ORDER BY created_at DESC LIMIT 200").all()));
app.post("/api/nearby", (req, res) => {
  const { name, kind, lat, lon, meta } = req.body;
  const id = uid();
  db.prepare("INSERT INTO nearby (id,name,kind,lat,lon,meta,created_at) VALUES (?,?,?,?,?,?,?)").run(id, name, kind, lat, lon, JSON.stringify(meta || {}), Date.now());
  io.emit("nearby:new", { id, name, kind, lat, lon });
  res.json({ ok: true, id });
});

// ---- Contributors / leaderboard ----
function bumpContributor(name) {
  const existing = db.prepare("SELECT * FROM contributors WHERE name=?").get(name);
  if (existing) db.prepare("UPDATE contributors SET reports=reports+1, points=points+10 WHERE name=?").run(name);
  else db.prepare("INSERT INTO contributors (id,name,points,reports) VALUES (?,?,?,?)").run(uid(), name, 10, 1);
}
app.get("/api/contributors", (req, res) => res.json(db.prepare("SELECT * FROM contributors ORDER BY points DESC LIMIT 50").all()));

// ---- CCTV registry ----
app.get("/api/cctv", (req, res) => res.json(db.prepare("SELECT * FROM cctv").all()));
app.post("/api/cctv", (req, res) => {
  const { name, url, lat, lon } = req.body;
  const id = uid();
  db.prepare("INSERT INTO cctv (id,name,url,lat,lon) VALUES (?,?,?,?,?)").run(id, name, url, lat, lon);
  res.json({ ok: true, id });
});

// ---- Media upload + live feed ----
app.post("/api/upload", upload.single("file"), (req, res) => {
  if (!req.file) return res.status(400).json({ error: "no file" });
  const { author = "anon", kind = "image", lat, lon, caption = "" } = req.body;
  const id = uid();
  const isVideo = (req.file.mimetype || "").startsWith("video");
  db.prepare("INSERT INTO media_feed (id,author,filename,kind,lat,lon,caption,created_at) VALUES (?,?,?,?,?,?,?,?)")
    .run(id, author, req.file.filename, isVideo ? "video" : "image", lat ? parseFloat(lat) : null, lon ? parseFloat(lon) : null, caption, Date.now());
  const row = db.prepare("SELECT * FROM media_feed WHERE id=?").get(id);
  io.emit("media:new", row);
  res.json(row);
});

app.get("/api/feed", (req, res) =>
  res.json(db.prepare("SELECT * FROM media_feed ORDER BY created_at DESC LIMIT 100").all())
);

app.use("/media", express.static(UPLOAD_DIR));

// ---- Public website (live view) ----
app.use(express.static(path.join(process.cwd(), "public")));

// ---- Health ----
app.get("/api/health", (req, res) => res.json({ name: "MapAi Backend", status: "ok", time: Date.now() }));

// ---- Places (nearby POI) ----
app.get("/api/places", (req, res) => {
  const { lat, lon, radius = 5, category } = req.query;
  let rows = db.prepare("SELECT * FROM places ORDER BY created_at DESC LIMIT 200").all();
  if (lat && lon) {
    const r = parseFloat(radius);
    rows = rows.filter(p2 => {
      const d = Math.hypot(p2.lat - parseFloat(lat), p2.lon - parseFloat(lon)) * 111;
      return d <= r && (!category || p2.category === category);
    });
  }
  res.json(rows);
});

app.post("/api/places", (req, res) => {
  const { name, category, lat, lon, rating = 4.0, is_open = true, fuel_price, extra } = req.body;
  const id = uid();
  db.prepare(`INSERT INTO places (id,name,category,lat,lon,rating,is_open,fuel_price,extra,created_at)
    VALUES (?,?,?,?,?,?,?,?,?,?)`).run(id, name, category, lat, lon, rating, is_open ? 1 : 0, fuel_price || null, extra || null, Date.now());
  const row = db.prepare("SELECT * FROM places WHERE id=?").get(id);
  io.emit("place:new", row);
  res.json(row);
});

// ---- Directions proxy (OSRM open-source by default; plug Google if key provided) ----
app.get("/api/directions", (req, res) => {
  const { from, to, profile = "driving" } = req.query;
  if (!from || !to) return res.status(400).json({ error: "from and to required" });
  const [lat1, lon1] = from.split(",").map(Number);
  const [lat2, lon2] = to.split(",").map(Number);
  const googleKey = process.env.GOOGLE_MAPS_API_KEY;
  if (googleKey) {
    const url = `https://maps.googleapis.com/maps/api/directions/json?origin=${lat1},${lon1}&destination=${lat2},${lon2}&key=${googleKey}`;
    fetch(url).then(r => r.json()).then(data => res.json(data)).catch(() => res.json({ error: "directions_failed" }));
  } else {
    const osrm = `https://router.project-osrm.org/route/v1/${profile}/${lon1},${lat1};${lon2},${lat2}?overview=full&geometries=geojson`;
    fetch(osrm).then(r => r.json()).then(data => res.json(data)).catch(() => res.json({ error: "directions_failed" }));
  }
});

io.on("connection", (socket) => {
  socket.on("ping", () => socket.emit("pong", Date.now()));
});

server.listen(PORT, () => console.log(`MapAi backend listening on :${PORT}`));
