@echo off
echo Iniciando geracao de App Bundle de TESTE (avonBrochure)...

echo.
echo ============================================================
echo Gerando Bundle para: avonBrochure
echo ============================================================
call gradlew.bat :app:bundleAvonBrochureRelease
if errorlevel 1 (
    echo [ERRO] Falha ao gerar bundle para avonBrochure
    pause
    exit /b 1
)

echo.
echo ============================================================
echo Concluido! O bundle de teste foi gerado.
echo ============================================================
pause
