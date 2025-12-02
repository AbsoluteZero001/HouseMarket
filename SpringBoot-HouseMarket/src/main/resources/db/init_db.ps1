# 初始化预约表结构和数据的PowerShell脚本

Write-Host "正在初始化预约表结构和数据..." -ForegroundColor Green

# 获取当前目录
$CurrentDir = Split-Path $MyInvocation.MyCommand.Path

# 执行表结构创建
Write-Host "正在创建预约表结构..." -ForegroundColor Yellow
Get-Content "$CurrentDir\schema.sql" | mysql -u root -p housemarket

# 检查上一个命令是否执行成功
if ($LASTEXITCODE -eq 0) {
    Write-Host "预约表结构创建成功!" -ForegroundColor Green
    
    # 执行数据插入
    Write-Host "正在插入预约表数据..." -ForegroundColor Yellow
    Get-Content "$CurrentDir\data.sql" | mysql -u root -p housemarket
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "预约表数据插入成功!" -ForegroundColor Green
        Write-Host "预约表初始化完成！" -ForegroundColor Green
    } else {
        Write-Host "预约表数据插入失败!" -ForegroundColor Red
    }
} else {
    Write-Host "预约表结构创建失败!" -ForegroundColor Red
}

Write-Host "按任意键退出..."
$Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")