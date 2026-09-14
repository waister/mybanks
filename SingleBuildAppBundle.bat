@echo off
echo Iniciando geracao de App Bundle (Release)...

echo.
echo ============================================================
echo Gerando Bundle Release: MyBanks
echo ============================================================
call gradlew.bat :app:bundleRelease
if errorlevel 1 (
    echo [ERRO] Falha ao gerar bundle release
    pause
    exit /b 1
)

echo.
echo ============================================================
echo Concluido! O bundle de release foi gerado.
echo ============================================================
pause
