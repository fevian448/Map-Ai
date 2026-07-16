package com.example.mapai.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.mapai.data.remote.ApiClient
import com.example.mapai.data.remote.ChatMessage
import com.example.mapai.data.remote.ChatRequest
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    val messages = remember {
        mutableStateListOf(ChatMessage("assistant", "Halo! Saya asisten AI MapAi. Tanya soal rute, macet, atau darurat."))
    }
    var input by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Text(
                        msg.content,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                            .widthIn(max = 320.dp),
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Tanya soal rute, macet, darurat…") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                val text = input.trim()
                if (text.isBlank()) return@IconButton
                input = ""
                messages.add(ChatMessage("user", text))
                scope.launch {
                    try {
                        val resp = ApiClient.api().chat(ChatRequest(listOf(ChatMessage("user", text))))
                        messages.add(ChatMessage("assistant", resp.content))
                    } catch (_: Exception) {
                        messages.add(ChatMessage("assistant", localFallback(text)))
                    }
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Kirim")
            }
        }
    }

    LaunchedEffect(messages.size) { listState.animateScrollToItem(messages.size) }
}

fun localFallback(text: String): String {
    val t = text.lowercase()
    return when {
        t.contains("macet") || t.contains("traffic") -> "Cek tab Laporan untuk info macet terdekat. Hindari ruas utama saat jam sibuk."
        t.contains("rute") || t.contains("route") || t.contains("jalan") -> "Pilih tujuan di Peta lalu ketuk 'Mulai Navigasi' untuk rute terbaik."
        t.contains("sos") || t.contains("darurat") -> "Buka menu Darurat/SOS, lalu tekan tombol merah untuk kirim lokasi ke kontak darurat."
        t.contains("bensin") || t.contains("bbm") || t.contains("fuel") -> "Buka Jelajah lalu pilih kategori BBM untuk stasiun terdekat."
        else -> "Saya asisten MapAi. Saya bantu soal navigasi, lalu lintas, dan fitur darurat. Tanya apa saja!"
    }
}
