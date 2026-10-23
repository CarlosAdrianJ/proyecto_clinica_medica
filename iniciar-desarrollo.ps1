$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path -Parent $MyInvocation.MyCommand.Path

$dbSegura = Read-Host 'Contrasena de MySQL root' -AsSecureString
$dbPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbSegura)

try {
    $env:DB_PASSWORD =
        [Runtime.InteropServices.Marshal]::PtrToStringBSTR($dbPtr)
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($dbPtr)
}

$jwtBytes = New-Object byte[] 32
$generador =
    [System.Security.Cryptography.RandomNumberGenerator]::Create()
$generador.GetBytes($jwtBytes)
$generador.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)

Start-Process `
    -FilePath 'cmd.exe' `
    -WorkingDirectory $raizProyecto `
    -ArgumentList '/k', 'backend\mvnw.cmd -f backend\pom.xml spring-boot:run'

Start-Process `
    -FilePath 'cmd.exe' `
    -WorkingDirectory $raizProyecto `
    -ArgumentList '/k', 'npm --prefix frontend run dev'

Write-Host 'Backend y frontend iniciados en ventanas separadas.'
Write-Host 'Espere unos segundos antes de usar la pagina.'

Start-Sleep -Seconds 12
Start-Process 'http://localhost:5173'