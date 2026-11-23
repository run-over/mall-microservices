@echo off
REM =====================================================================
REM Create the 7 databases of the mall system
REM Usage: tools\init-database.bat [mysql-user] [mysql-password] [host] [port]
REM =====================================================================
setlocal
set MYSQL_USER=%1
if "%MYSQL_USER%"=="" set MYSQL_USER=root
set MYSQL_PWD_ARG=%2
if "%MYSQL_PWD_ARG%"=="" set MYSQL_PWD_ARG=123456
set HOST=%3
if "%HOST%"=="" set HOST=127.0.0.1
set PORT=%4
if "%PORT%"=="" set PORT=3306

cd /d "%~dp0..\sql"
for %%f in (01_mall_user.sql 02_mall_product.sql 03_mall_stock.sql 04_mall_order.sql 05_mall_payment.sql 06_mall_delivery.sql 07_mall_auth.sql) do (
  echo executing %%f
  mysql -h%HOST% -P%PORT% -u%MYSQL_USER% -p%MYSQL_PWD_ARG% --default-character-set=utf8mb4 < %%f
  if errorlevel 1 echo   [FAILED] %%f
)
echo All databases created.
endlocal
pause
