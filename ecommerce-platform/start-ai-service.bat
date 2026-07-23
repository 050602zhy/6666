@echo off
chcp 65001 >nul
echo ============================================
echo  启动 AI Service (自动处理端口冲突)
echo ============================================

REM 查找并停止占用8081的进程
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do (
    echo 发现占用8081端口的进程 PID=%%a，正在停止...
    taskkill /F /PID %%a >nul 2>&1
    echo 已停止进程 %%a
)

echo.
echo 正在启动 ai-service...
cd /d "%~dp0\backend"
call mvn spring-boot:run -pl ai-service -DskipTests

pause