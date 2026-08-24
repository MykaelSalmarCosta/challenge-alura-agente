# dev.ps1 — sobe backend e frontend juntos
# Uso: .\dev.ps1
# Pré-requisitos:
#   - Node.js instalado (para o frontend)
#   - Java 21 instalado (para o backend)
#   - Chave da Cohere em backend/src/main/resources/application-local.properties

Write-Host "=== Iniciando Agente SPS ===" -ForegroundColor Cyan

# Backend (Spring Boot na porta 8080, perfil local carrega a chave do properties)
$backend = Start-Process -PassThru -NoNewWindow -FilePath "cmd" `
    -ArgumentList "/c cd backend && mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local" `
    -WorkingDirectory $PSScriptRoot

# Aguarda o backend começar a subir
Start-Sleep -Seconds 3

# Frontend (Vite na porta 5173)
$frontend = Start-Process -PassThru -NoNewWindow -FilePath "cmd" `
    -ArgumentList "/c cd frontend && npm run dev" `
    -WorkingDirectory $PSScriptRoot

Write-Host ""
Write-Host "Backend:  http://localhost:8080" -ForegroundColor Green
Write-Host "Frontend: http://localhost:5173" -ForegroundColor Green
Write-Host ""
Write-Host "Pressione Ctrl+C para encerrar ambos." -ForegroundColor Yellow

try {
    Wait-Process -Id $backend.Id
} finally {
    if (!$frontend.HasExited) { Stop-Process -Id $frontend.Id -Force }
    if (!$backend.HasExited) { Stop-Process -Id $backend.Id -Force }
}
