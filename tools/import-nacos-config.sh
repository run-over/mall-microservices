#!/usr/bin/env bash
# =====================================================================
# 一键导入 Nacos 配置（Linux / macOS / Git Bash）
# 用法：bash tools/import-nacos-config.sh [nacos地址] [命名空间ID]
# =====================================================================
set -e

NACOS=${1:-http://127.0.0.1:8848}
NS=${2:-mall-dev}
GROUP=MALL_GROUP

cd "$(dirname "$0")/../nacos-config"

echo "[1/3] 创建命名空间 $NS"
curl -s -X POST "$NACOS/nacos/v1/console/namespaces" \
  --data-urlencode "customNamespaceId=$NS" \
  --data-urlencode "namespaceName=$NS" \
  --data-urlencode "namespaceDesc=development" >/dev/null

echo "[2/3] 导入业务配置到 $GROUP"
for f in mall-*.yaml mall-*.json; do
  code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$NACOS/nacos/v1/cs/configs" \
    --data-urlencode "dataId=$f" \
    --data-urlencode "group=$GROUP" \
    --data-urlencode "tenant=$NS" \
    --data-urlencode "type=$( [ "${f##*.}" = "json" ] && echo json || echo yaml )" \
    --data-urlencode "content@$f")
  echo "  $f -> HTTP $code"
done

echo "[3/3] 导入 Seata Server 配置到 SEATA_GROUP"
code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$NACOS/nacos/v1/cs/configs" \
  --data-urlencode "dataId=seataServer.properties" \
  --data-urlencode "group=SEATA_GROUP" \
  --data-urlencode "tenant=$NS" \
  --data-urlencode "type=properties" \
  --data-urlencode "content@seataServer.properties")
echo "  seataServer.properties -> HTTP $code"

echo "配置导入完成，请在 Nacos 控制台确认：命名空间 $NS 下共 $(ls mall-* | wc -l) 条业务配置"
