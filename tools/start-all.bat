@echo off
rem =====================================================================
rem  Start all mall-cloud services in separate console windows (Windows)
rem  Prerequisite: Nacos(8848) and Seata(8091) are already running,
rem                and the project has been packaged with: mvn -DskipTests package
rem  Usage: double click, or run  tools\start-all.bat
rem =====================================================================
setlocal
cd /d "%~dp0.."

rem clear host-injected port variable to avoid overriding bootstrap.yml
set "SERVER__PORT="

if not exist "mall-gateway\target\mall-gateway-1.0.0.jar" (
    echo [ERROR] jar not found, please run: mvn -DskipTests package
    pause
    exit /b 1
)

call :run mall-user      8081
call :run mall-product   8082
call :run mall-stock     8083
call :run mall-order     8084
call :run mall-payment   8085
call :run mall-delivery  8086
call :run mall-auth      9000
call :run mall-admin     8090
timeout /t 20 /nobreak >nul
call :run mall-gateway   8080

echo.
echo All services launched. Gateway: http://127.0.0.1:8080
echo Logs are printed in each console window.
pause
exit /b 0

:run
start "%~1" cmd /c "java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar %~1\target\%~1-1.0.0.jar --server.port=%~2"
timeout /t 3 /nobreak >nul
exit /b 0
