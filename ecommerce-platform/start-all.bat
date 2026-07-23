@echo off
chcp 65001 >nul
echo =========================================
echo   智能电商运营平台 - 一键启动脚本
echo =========================================
echo.

REM 检查必要服务端口是否被占用
echo [1/6] 检查端口占用...
netstat -ano | findstr ":8080" >nul && echo 警告: 8080端口被占用 && goto :error
netstat -ano | findstr ":8081" >nul && echo 警告: 8081端口被占用 && goto :error
netstat -ano | findstr ":8082" >nul && echo 警告: 8082端口被占用 && goto :error
netstat -ano | findstr ":3306" >nul && echo 警告: 3306端口被占用 && goto :error
netstat -ano | findstr ":8848" >nul && echo 警告: 8848端口被占用 && goto :error
netstat -ano | findstr ":8000" >nul && echo 警告: 8000端口被占用 && goto :error
echo 端口检查通过
echo.

REM 检查 MySQL
echo [2/6] 检查 MySQL...
mysql -h127.0.0.1 -uroot -p050602zhy -e "SELECT 1" >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: MySQL 未启动或密码不正确
    echo 请先启动 MySQL 服务
    goto :error
)
echo MySQL 连接正常
echo.

REM 检查 Nacos
echo [3/6] 检查 Nacos...
curl -s http://127.0.0.1:8848/nacos >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Nacos 未启动
    echo 请先启动 Nacos (startup.cmd -m standalone)
    goto :error
)
echo Nacos 连接正常
echo.

REM 检查 ChromaDB
echo [4/6] 检查 ChromaDB...
curl -s http://127.0.0.1:8000/api/v1/heartbeat >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: ChromaDB 未启动
    echo 请先启动 ChromaDB (chroma run --path ./chroma_data)
    goto :error
)
echo ChromaDB 连接正常
echo.

REM 编译项目
echo [5/6] 编译后端项目...
cd backend
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 编译失败
    goto :error
)
cd ..
echo 编译完成
echo.

REM 启动服务
echo [6/6] 启动服务...
echo.

echo 启动 Gateway (端口8080)...
start "Gateway" cmd /k "cd backend && mvn spring-boot:run -pl gateway -DskipTests"
timeout /t 15 /nobreak >nul

echo 启动 Biz-Service (端口8082)...
start "Biz-Service" cmd /k "cd backend && mvn spring-boot:run -pl biz-service -DskipTests"
timeout /t 15 /nobreak >nul

echo 启动 AI-Service (端口8081)...
start "AI-Service" cmd /k "cd backend && mvn spring-boot:run -pl ai-service -DskipTests"
timeout /t 15 /nobreak >nul

echo.
echo =========================================
echo   服务启动完成！
echo =========================================
echo.
echo 访问地址:
echo   前端页面: http://localhost:3000
echo   API网关:  http://localhost:8080
echo   业务服务: http://localhost:8082
echo   AI服务:   http://localhost:8081
echo.
echo 按任意键运行全链路验收测试...
pause >nul

call python scripts\acceptance-test.py --host localhost --port 8080

goto :end

:error
echo.
echo 启动失败，请检查上述错误信息
echo.
pause
exit /b 1

:end
echo.
pause
