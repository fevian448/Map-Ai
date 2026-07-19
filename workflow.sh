#!/bin/bash
# MapAi Terminal Integration Script
# Run from: /home/tukuk/AndroidStudioProjects/MapAi/
# Usage: ./workflow.sh [command]

set -e

PROJECT="/home/tukuk/AndroidStudioProjects/MapAi"
WIKI="/home/tukuk/AndroidStudioProjects/MapAi-wiki"
REPO="fevian448/Map-Ai"
PAGES_URL="https://fevian448.github.io/Map-Ai/"
ACTIONS_URL="https://github.com/fevian448/Map-Ai/actions"
WIKI_URL="https://github.com/fevian448/Map-Ai/wiki"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

info() { echo -e "${BLUE}[INFO]${NC} $1"; }
ok() { echo -e "${GREEN}[OK]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
err() { echo -e "${RED}[ERROR]${NC} $1"; }
banner() { echo -e "${PURPLE}$1${NC}"; }

show_header() {
    clear
    banner "=========================================="
    banner "  MapAi — Android Studio + GitHub"
    banner "=========================================="
    echo ""
}

show_status() {
    show_header
    cd "$PROJECT"
    
    banner "📊 PROJECT STATUS"
    echo ""
    info "Repository: $REPO"
    info "Local Path: $PROJECT"
    echo ""
    
    info "Git Branch:"
    git branch -v
    echo ""
    
    info "Recent Commits:"
    git log --oneline -5
    echo ""
    
    info "Git Status:"
    git status --short | head -20
    echo ""
    
    info "Backend:"
    if [ -f "$PROJECT/backend/package.json" ]; then
        ok "Backend configured"
    else
        err "Backend not found"
    fi
    echo ""
    
    info "Android:"
    if [ -f "$PROJECT/app/build.gradle.kts" ]; then
        ok "Android project configured"
    else
        err "Android project not found"
    fi
    echo ""
    
    info "GitHub Pages:"
    echo "  URL: $PAGES_URL"
    echo ""
    
    info "GitHub Actions:"
    echo "  URL: $ACTIONS_URL"
    echo ""
    
    info "Wiki:"
    echo "  URL: $WIKI_URL"
    echo ""
}

build_android() {
    show_header
    cd "$PROJECT"
    
    banner "🔨 BUILDING ANDROID APP"
    echo ""
    
    info "Cleaning previous builds..."
    ./gradlew clean
    
    info "Building Debug APK..."
    ./gradlew assembleDebug
    
    info "Building Release APK..."
    ./gradlew assembleRelease
    
    ok "Build complete!"
    echo ""
    info "APK Locations:"
    echo "  Debug:   $PROJECT/app/build/outputs/apk/debug/app-debug.apk"
    echo "  Release: $PROJECT/app/build/outputs/apk/release/app-release.apk"
    echo ""
}

push_github() {
    show_header
    cd "$PROJECT"
    
    banner "📤 PUSHING TO GITHUB"
    echo ""
    
    info "Staging all changes..."
    git add -A
    
    info "Committing..."
    if git diff --staged --quiet; then
        warn "Nothing to commit"
    else
        git commit -m "Update MapAi — $(date '+%Y-%m-%d %H:%M')"
    fi
    
    info "Pushing to origin/master..."
    git push origin master
    
    ok "Push complete!"
    echo ""
    info "View at: $ACTIONS_URL"
    echo ""
}

deploy_backend() {
    show_header
    banner "🚀 DEPLOYING BACKEND"
    echo ""
    
    cd "$PROJECT/backend"
    
    if [ ! -f "package.json" ]; then
        err "Backend not found at $PROJECT/backend"
        exit 1
    fi
    
    info "Installing dependencies..."
    npm install
    
    info "Starting backend server..."
    ok "Backend starting at http://localhost:3000"
    echo ""
    info "Live View: http://localhost:3000/"
    info "API Docs:  http://localhost:3000/api/health"
    echo ""
    info "Press Ctrl+C to stop"
    echo ""
    
    npm start
}

deploy_live() {
    show_header
    banner "🌐 STARTING LIVE VIEW"
    echo ""
    
    cd "$PROJECT/backend"
    
    if [ ! -f "package.json" ]; then
        err "Backend not found"
        exit 1
    fi
    
    info "Installing dependencies..."
    npm install
    
    info "Starting server with live view..."
    ok "Live view available at http://localhost:3000/"
    echo ""
    info "Press Ctrl+C to stop"
    echo ""
    
    npm start
}

push_wiki() {
    show_header
    banner "📚 PUSHING WIKI"
    echo ""
    
    if [ ! -d "$WIKI/.git" ]; then
        err "Wiki not initialized at $WIKI"
        info "Please create wiki first at: $WIKI_URL"
        exit 1
    fi
    
    cd "$WIKI"
    
    info "Wiki status:"
    git status --short
    echo ""
    
    info "Pushing to GitHub Wiki..."
    git push origin master
    
    ok "Wiki updated!"
    echo ""
    info "View at: $WIKI_URL"
    echo ""
}

full_workflow() {
    show_header
    banner "🔄 FULL WORKFLOW: BUILD + PUSH + DEPLOY"
    echo ""
    
    # Build
    info "[1/3] Building Android app..."
    cd "$PROJECT"
    ./gradlew clean assembleDebug assembleRelease
    ok "Build complete!"
    echo ""
    
    # Push
    info "[2/3] Pushing to GitHub..."
    git add -A
    git commit -m "Update MapAi — $(date '+%Y-%m-%d %H:%M')" || warn "Nothing to commit"
    git push origin master
    ok "Push complete!"
    echo ""
    
    # Deploy
    info "[3/3] Starting backend + live view..."
    cd "$PROJECT/backend"
    npm install 2>/dev/null || true
    ok "Starting server..."
    echo ""
    info "Live view: http://localhost:3000/"
    info "GitHub Actions: $ACTIONS_URL"
    echo ""
    info "Press Ctrl+C to stop"
    echo ""
    
    npm start
}

monitor_actions() {
    show_header
    banner "📊 MONITORING GITHUB ACTIONS"
    echo ""
    
    info "Opening GitHub Actions in browser..."
    xdg-open "$ACTIONS_URL" 2>/dev/null || echo "Visit: $ACTIONS_URL"
    
    ok "Opened browser"
    echo ""
}

open_pages() {
    show_header
    banner "🌐 OPENING GITHUB PAGES"
    echo ""
    
    info "Live View URL: $PAGES_URL"
    xdg-open "$PAGES_URL" 2>/dev/null || echo "Visit: $PAGES_URL"
    
    ok "Opened browser"
    echo ""
}

show_help() {
    show_header
    banner "📖 COMMANDS"
    echo ""
    echo "  status     Show project status, git info, URLs"
    echo "  build      Build Android APK (debug + release)"
    echo "  push       Git add, commit, and push to GitHub"
    echo "  deploy     Deploy backend locally (localhost:3000)"
    echo "  live       Start live view server"
    echo "  wiki       Push wiki content to GitHub"
    echo "  all        Full workflow: build + push + deploy"
    echo "  monitor    Open GitHub Actions in browser"
    echo "  pages      Open GitHub Pages live view"
    echo "  help       Show this help"
    echo ""
    banner "🔗 QUICK LINKS"
    echo ""
    echo "  Repo:      https://github.com/$REPO"
    echo "  Pages:     $PAGES_URL"
    echo "  Actions:   $ACTIONS_URL"
    echo "  Wiki:      $WIKI_URL"
    echo ""
}

# Main
case "${1:-help}" in
    status)
        show_status
        ;;
    build)
        build_android
        ;;
    push)
        push_github
        ;;
    deploy)
        deploy_backend
        ;;
    live)
        deploy_live
        ;;
    wiki)
        push_wiki
        ;;
    all)
        full_workflow
        ;;
    monitor)
        monitor_actions
        ;;
    pages)
        open_pages
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        err "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
