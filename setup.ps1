# Prepara Python + venv para cleanyourtv y prueba la conexion con la tele.
# Uso (PowerShell en esta carpeta):  powershell -ExecutionPolicy Bypass -File .\setup.ps1
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$py = Get-Command py -ErrorAction SilentlyContinue
if (-not $py) { $py = Get-Command python -ErrorAction SilentlyContinue }
if (-not $py -or $py.Source -like '*WindowsApps*') {
    Write-Host 'Instalando Python 3.12 con winget...'
    winget install -e --id Python.Python.3.12 --accept-source-agreements --accept-package-agreements
    $env:Path = [Environment]::GetEnvironmentVariable('Path','Machine') + ';' + [Environment]::GetEnvironmentVariable('Path','User')
    $py = Get-Command py -ErrorAction SilentlyContinue
    if (-not $py) { $py = Get-Command python }
}
& $py.Source --version
if (-not (Test-Path tools\venv\Scripts\python.exe)) { & $py.Source -m venv tools\venv }
tools\venv\Scripts\python.exe -m pip install -q -r tools\requirements.txt
$env:TV_HOST = '192.168.1.188'
Write-Host '>>> Ahora debe salir "Permitir depuracion USB?" en la tele: marca "Permitir siempre" y acepta.'
tools\venv\Scripts\python.exe -I tools\tv.py --probe
