@echo off

REM 检查Java是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java运行时环境，请先安装JRE 1.8或更高版本
    pause
    exit /b 1
)

echo 启动OpenClaw应用程序...
echo 请稍候，正在加载...

REM 运行应用程序
java -jar openclaw4j-1.0-SNAPSHOT-jar-with-dependencies.jar

REM 暂停以查看输出
pause