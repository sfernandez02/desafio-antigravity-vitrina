# ==============================================================================
# verify.ps1 — Arnés de Verificación de Cumplimiento (AGENTS.md)
# ==============================================================================

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$failures = 0
$checks = 0

function Report-Check {
    param(
        [string]$Name,
        [bool]$Condition,
        [string]$Details
    )
    $script:checks++
    if ($Condition) {
        Write-Host " [PASS] $Name" -ForegroundColor Green
    } else {
        $script:failures++
        Write-Host " [FAIL] $Name - $Details" -ForegroundColor Red
    }
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  EJECUTANDO VERIFICACIÓN DE CUMPLIMIENTO DE AGENTS.md    " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$appJsPath = Join-Path $root "mockup\app.js"
$indexHtmlPath = Join-Path $root "mockup\index.html"
$catalogCsvPath = Join-Path $root "data\catalog.csv"
$backendDir = Join-Path $root "backend"

$appJs = Get-Content -Raw -Path $appJsPath
$indexHtml = Get-Content -Raw -Path $indexHtmlPath

# ------------------------------------------------------------------------------
# 1. Verificación: No descargar el catálogo completo en el frontend
# ------------------------------------------------------------------------------
$loadsCsvInFrontend = ($appJs -match "catalog\.csv") -or ($indexHtml -match "catalog\.csv")
Report-Check `
    -Name "Regla 1.1: Frontend NO carga ni referencia directamente catalog.csv" `
    -Condition (-not $loadsCsvInFrontend) `
    -Details "Se detectó referencia a catalog.csv en los archivos del frontend."

$hasUnboundedFetch = ($appJs -match "size\s*=\s*(4032|all|9999)")
Report-Check `
    -Name "Regla 1.2: Frontend solicita páginas acotadas (sin tamaño masivo ni desbordado)" `
    -Condition (-not $hasUnboundedFetch) `
    -Details "Se detectó intento de solicitar el catálogo completo en fetch."

# ------------------------------------------------------------------------------
# 2. Verificación: No mover lógica de negocio al frontend (filtros/paginación/búsqueda)
# ------------------------------------------------------------------------------
$hasClientFilter = ($appJs -match "\.filter\s*\(") -or ($appJs -match "filtered\s*=\s*")
Report-Check `
    -Name "Regla 2.1: Frontend NO filtra productos en memoria (.filter / filtered)" `
    -Condition (-not $hasClientFilter) `
    -Details "Se detectó lógica de filtrado de productos dentro de app.js."

$hasClientSlice = ($appJs -match "products\.slice\s*\(") -or ($appJs -match "items\.slice\s*\(")
Report-Check `
    -Name "Regla 2.2: Frontend NO pagina productos en memoria (.slice)" `
    -Condition (-not $hasClientSlice) `
    -Details "Se detectó lógica de paginación client-side en app.js."

$sendsQueryParams = ($appJs -match "params\.append\('search'") -and ($appJs -match "params\.append\('category'") -and ($appJs -match "params\.append\('format'")
Report-Check `
    -Name "Regla 2.3: Búsqueda y filtros delegan a parámetros de consulta hacia el backend" `
    -Condition $sendsQueryParams `
    -Details "No se encontraron los parámetros 'search', 'category', 'format' enviados a la API."

# ------------------------------------------------------------------------------
# 3. Verificación: No inventar datos
# ------------------------------------------------------------------------------
$hasHardcodedProducts = ($appJs -match "const\s+products\s*=\s*\[") -or ($appJs -match "let\s+products\s*=\s*\[")
Report-Check `
    -Name "Regla 3.1: Frontend NO contiene arrays hardcodeados ni datos inventados" `
    -Condition (-not $hasHardcodedProducts) `
    -Details "Se encontró un array estático de productos en app.js."

$csvLineCount = (Get-Content -Path $catalogCsvPath).Length
Report-Check `
    -Name "Regla 3.2: Integridad del catálogo real (4.032 productos + 1 cabecera = 4.033 líneas)" `
    -Condition ($csvLineCount -eq 4033) `
    -Details "El archivo catalog.csv contiene $csvLineCount líneas en lugar de 4.033."

# ------------------------------------------------------------------------------
# 4. Verificación: Paginación proveniente de Spring Boot / H2
# ------------------------------------------------------------------------------
$consumesBackendPagination = ($appJs -match "data\.totalPages") -and ($appJs -match "data\.totalElements") -and ($appJs -match "data\.page")
Report-Check `
    -Name "Regla 4.1: Paginación del frontend depende estrictamente de metadatos de la API" `
    -Condition $consumesBackendPagination `
    -Details "app.js no consume totalPages, totalElements o page devueltos por el backend."

# ------------------------------------------------------------------------------
# 5. Verificación: Tests automatizados del Backend (Spring Boot + H2)
# ------------------------------------------------------------------------------
Write-Host "`nEjecutando pruebas de backend (mvn test -o)..." -ForegroundColor Yellow
$mvnOutput = & mvn test -o -f (Join-Path $backendDir "pom.xml") 2>&1 | Out-String
$mvnSuccess = ($LASTEXITCODE -eq 0) -and ($mvnOutput -match "BUILD SUCCESS")
Report-Check `
    -Name "Regla 5.1: Suite completa de pruebas Spring Boot (16 pruebas MockMvc/Service/H2)" `
    -Condition $mvnSuccess `
    -Details "Las pruebas de backend fallaron:`n$mvnOutput"

# ------------------------------------------------------------------------------
# Resumen
# ------------------------------------------------------------------------------
Write-Host "==========================================================" -ForegroundColor Cyan
if ($failures -eq 0) {
    Write-Host " VERIFICACIÓN EXITOSA: Todas las reglas ($checks/$checks) se cumplen estrictamente." -ForegroundColor Green
    exit 0
} else {
    Write-Host " VERIFICACIÓN FALLIDA: $failures de $checks comprobaciones no pasaron." -ForegroundColor Red
    exit 1
}
