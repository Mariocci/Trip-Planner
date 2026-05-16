# Generate JWT Secret (256 bits / 32 bytes)
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$secret = [Convert]::ToBase64String($bytes)

Write-Host "Generated JWT Secret (copy this to your .env file):" -ForegroundColor Green
Write-Host $secret -ForegroundColor Yellow
Write-Host ""
Write-Host "Add this to your .env file:" -ForegroundColor Green
Write-Host "JWT_SECRET=$secret" -ForegroundColor Yellow
