-- =====================================================================
-- 一键初始化脚本
-- 用法（在 mall-cloud/sql 目录下执行）：
--   mysql -uroot -p123456 < 00_init_all.sql
-- 注意：请先在当前目录下执行，source 为相对路径
-- =====================================================================
source 01_mall_user.sql;
source 02_mall_product.sql;
source 03_mall_stock.sql;
source 04_mall_order.sql;
source 05_mall_payment.sql;
source 06_mall_delivery.sql;
source 07_mall_auth.sql;
