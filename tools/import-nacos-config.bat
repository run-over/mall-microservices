@echo off
REM =====================================================================
REM Import Nacos configs on Windows (curl 10+ built in Win10/11)
REM Usage: tools\import-nacos-config.bat [nacos-url] [namespace]
REM =====================================================================
setlocal
set NACOS=%1
if "%NACOS%"=="" set NACOS=http://127.0.0.1:8848
set NS=%2
if "%NS%"=="" set NS=mall-dev
set GROUP=MALL_GROUP

cd /d "%~dp0..\nacos-config"

echo [1/3] create namespace %NS%
curl -s -X POST "%NACOS%/nacos/v1/console/namespaces" --data-urlencode "customNamespaceId=%NS%" --data-urlencode "namespaceName=%NS%" --data-urlencode "namespaceDesc=development" >nul

echo [2/3] import business configs into %GROUP%
for %%f in (mall-*.yaml mall-*.json) do (
  curl -s -o nul -X POST "%NACOS%/nacos/v1/cs/configs" --data-urlencode "dataId=%%f" --data-urlencode "group=%GROUP%" --data-urlencode "tenant=%NS%" --data-urlencode "content@%%f"
  echo   imported %%f
)

echo [3/3] import seata server config into SEATA_GROUP
curl -s -o nul -X POST "%NACOS%/nacos/v1/cs/configs" --data-urlencode "dataId=seataServer.properties" --data-urlencode "group=SEATA_GROUP" --data-urlencode "tenant=%NS%" --data-urlencode "content@seataServer.properties"
echo   imported seataServer.properties

echo Done. Please check namespace %NS% in Nacos console.
endlocal
pause
