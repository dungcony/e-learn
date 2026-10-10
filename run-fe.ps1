# Chay FE (Vite) tai http://localhost:3000. Dung bang Ctrl+C.
# Thong bao dung tieng Viet khong dau vi Windows PowerShell 5.1 doc file UTF-8 khong BOM theo bang ma ANSI.

$ErrorActionPreference = 'Stop'

$feDir = Join-Path $PSScriptRoot 'FE'

# dung throw thay vi exit de khong dong luon terminal khi script bi dot-source
try {
    if (-not (Get-Command node -ErrorAction SilentlyContinue)) { throw 'Khong tim thay node trong PATH.' }

    Push-Location $feDir
    try {
        if (-not (Test-Path 'node_modules')) {
            Write-Host 'Cai thu vien FE (npm install)...'
            npm install
            if ($LASTEXITCODE -ne 0) { throw 'npm install that bai.' }
        }
        npm run dev
    }
    finally {
        Pop-Location
    }
}
catch {
    Write-Host "[LOI] $($_.Exception.Message)" -ForegroundColor Red
    $global:LASTEXITCODE = 1
}
