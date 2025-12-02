@echo off
echo 正在初始化预约表结构和数据...

REM 获取当前目录
set CURRENT_DIR=%~dp0

REM 执行表结构创建
echo 正在创建预约表结构...
mysql -u root -p housemarket < "%CURRENT_DIR%schema.sql"

REM 执行数据插入
echo 正在插入预约表数据...
mysql -u root -p housemarket < "%CURRENT_DIR%data.sql"

echo 预约表初始化完成！
pause