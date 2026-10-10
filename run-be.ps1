# Chay BE (Spring Boot) tai http://localhost:8080. Dung bang Ctrl+C.
# Bien moi truong doc tu BE/.env.local (khong commit), mau o BE/.env.example.
# Thong bao dung tieng Viet khong dau vi Windows PowerShell 5.1 doc file UTF-8 khong BOM theo bang ma ANSI.

$ErrorActionPreference = 'Stop'

$beDir = Join-Path $PSScriptRoot 'BE'
$envFile = Join-Path $beDir '.env.local'
$envExample = Join-Path $beDir '.env.example'
$requiredVars = @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'REDIS_HOST', 'REDIS_PORT')

function Import-EnvFile([string]$path) {
    foreach ($line in Get-Content -Path $path -Encoding UTF8) {
        $trimmed = $line.Trim()
        if ($trimmed -eq '' -or $trimmed.StartsWith('#')) { continue }

        $idx = $trimmed.IndexOf('=')
        if ($idx -lt 1) { continue }

        $name = $trimmed.Substring(0, $idx).Trim()
        $value = $trimmed.Substring($idx + 1).Trim()
        $isQuoted = $value.Length -ge 2 -and (
            ($value.StartsWith('"') -and $value.EndsWith('"')) -or
            ($value.StartsWith("'") -and $value.EndsWith("'")))
        if ($isQuoted) { $value = $value.Substring(1, $value.Length - 2) }

        # bien de trong (VD JWT_SECRET=) duoc coi nhu chua khai bao
        if ($value -ne '') { Set-Item -Path "Env:$name" -Value $value }
    }
}

function New-JwtSecret {
    $bytes = New-Object byte[] 48
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    $rng.Dispose()
    return [Convert]::ToBase64String($bytes)
}

# dung throw thay vi exit de khong dong luon terminal khi script bi dot-source
try {
    if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw 'Khong tim thay java trong PATH (can Java 17).' }

    if (-not (Test-Path $envFile)) {
        Copy-Item -Path $envExample -Destination $envFile
        throw "Chua co $envFile. Da tao tu file mau, hay dien gia tri roi chay lai."
    }
    Import-EnvFile $envFile

    if (-not $env:JWT_SECRET) {
        $env:JWT_SECRET = New-JwtSecret
        Write-Host '[CANH BAO] JWT_SECRET chua khai bao, da sinh ngau nhien: token cu se het hieu luc moi lan chay lai.' -ForegroundColor Yellow
    }

    $missing = $requiredVars | Where-Object { -not (Get-Item -Path "Env:$_" -ErrorAction SilentlyContinue) }
    if ($missing) { throw ('Thieu bien trong BE/.env.local: ' + ($missing -join ', ')) }
    if (-not $env:DB_URL.StartsWith('jdbc:')) { throw 'DB_URL phai bat dau bang "jdbc:" (VD jdbc:postgresql://localhost:5432/quanlycuahang).' }

    Push-Location $beDir
    try {
        .\mvnw.cmd spring-boot:run
    }
    finally {
        Pop-Location
    }
}
catch {
    Write-Host "[LOI] $($_.Exception.Message)" -ForegroundColor Red
    $global:LASTEXITCODE = 1
}
