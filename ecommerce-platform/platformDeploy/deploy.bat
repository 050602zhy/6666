@echo off
chcp 65001 >nul
echo ================================
echo   Docker 容器化部署 - 一键脚本
echo ================================
echo.

REM 获取项目根目录（platformDeploy的上一级）
set PROJECT_ROOT=%~dp0..
echo 项目根目录: %PROJECT_ROOT%
echo.

REM 把 env.tmp 重命名为 .env
if exist "%~dp0env.tmp" (
    copy /Y "%~dp0env.tmp" "%~dp0.env" >nul 2>&1
    echo .env 文件已创建
)

echo [1/6] 编译 backend 模块...
cd /d "%PROJECT_ROOT%\backend"
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo backend 编译失败！
    pause
    exit /b 1
)
echo.

echo [2/6] 编译 gateway 模块...
cd /d "%PROJECT_ROOT%\gateway"
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo gateway 编译失败！
    pause
    exit /b 1
)
echo.

echo [3/6] 复制 jar 包到部署目录...
copy /Y "%PROJECT_ROOT%\gateway\target\gateway-1.0.0-SNAPSHOT.jar" "%~dp0gateway\" >nul 2>&1
copy /Y "%PROJECT_ROOT%\backend\biz-service\target\biz-service-1.0.0-SNAPSHOT.jar" "%~dp0biz-service\" >nul 2>&1
copy /Y "%PROJECT_ROOT%\backend\ai-service\target\ai-service-1.0.0-SNAPSHOT.jar" "%~dp0ai-service\" >nul 2>&1
echo jar 包复制完成
echo.

echo [4/6] 前端打包...
cd /d "%PROJECT_ROOT%\frontend"
call npm run build
if %errorlevel% neq 0 (
    echo 前端打包失败！
    pause
    exit /b 1
)
echo.

echo [5/6] 复制前端产物...
if exist "%~dp0frontend\dist" rmdir /S /Q "%~dp0frontend\dist"
xcopy /E /Y /I dist "%~dp0frontend\dist\" >nul 2>&1
echo.

echo [6/6] 启动 Docker 服务...
cd /d "%~dp0"
docker compose up -d --build
echo.
echo ================================
echo   部署完成！
echo ================================
echo.
echo   前端: http://localhost:3000
echo   网关: http://localhost:8080
echo   业务: http://localhost:8082
echo   AI:   http://localhost:8081
echo.
docker compose ps
pause
