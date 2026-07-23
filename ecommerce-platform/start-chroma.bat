@echo off
chcp 65001 >nul
echo ============================================
echo  启动 Chroma 向量数据库
echo ============================================

REM 检查是否已安装 chromadb
python -c "import chromadb" 2>nul
if errorlevel 1 (
    echo [INFO] chromadb 未安装，正在安装...
    pip install chromadb
    if errorlevel 1 (
        echo [ERROR] chromadb 安装失败，请检查 pip 环境
        pause
        exit /b 1
    )
    echo [INFO] chromadb 安装完成
) else (
    echo [INFO] chromadb 已安装
)

echo.
echo [INFO] 正在启动 Chroma 服务...
echo [INFO] 数据存储路径: %~dp0chroma_data
echo [INFO] 服务地址: http://127.0.0.1:8000
echo.

REM 创建数据目录
if not exist "%~dp0chroma_data" mkdir "%~dp0chroma_data"

REM 启动 Chroma
cd /d "%~dp0"
chroma run --path "./chroma_data" --host 127.0.0.1 --port 8000

pause