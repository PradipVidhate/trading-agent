$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $repoRoot

function Free-Port {
    param(
        [int]$Port
    )

    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $connections) {
        Write-Host "Port $Port is free."
        return
    }

    $processIds = $connections | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($processId in $processIds) {
        if ($processId -and $processId -ne 0) {
            $processInfo = Get-CimInstance Win32_Process -Filter "ProcessId = $processId"
            if (-not $processInfo -or $processInfo.CommandLine -notmatch 'com\.pradip\.tradingbot\.TradingAgentApplication') {
                $processName = if ($processInfo) { $processInfo.Name } else { 'unknown process' }
                throw "Port $Port is in use by $processName (PID $processId), not this Trading Agent. Stop it manually or choose another port."
            }

            Write-Host "Stopping existing Trading Agent process $processId on port $Port"
            Stop-Process -Id $processId -Force -ErrorAction Stop
        }
    }

    Write-Host "Port $Port has been released."
}

function Get-JavaHome {
    if ($env:JAVA_HOME -and (Test-Path $env:JAVA_HOME)) {
        return $env:JAVA_HOME
    }

    $javaRoot = 'C:\Program Files\Java'
    if (Test-Path $javaRoot) {
        $jdk = Get-ChildItem -Path $javaRoot -Directory -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending |
            Select-Object -First 1

        if ($jdk) {
            return $jdk.FullName
        }
    }

    throw 'Java JDK was not found. Please install Java 17/18/25 and try again.'
}

Write-Host 'Checking for stale process on port 8081...'
Free-Port -Port 8081

$javaHome = Get-JavaHome
$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"

Write-Host "Using Java from: $javaHome"
Write-Host 'Starting Spring Boot app...'
mvn spring-boot:run
