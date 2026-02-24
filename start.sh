#!/bin/bash

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java运行时环境，请先安装JRE 1.8或更高版本"
    exit 1
fi

echo "启动OpenClaw应用程序..."
echo "请稍候，正在加载..."

# 运行应用程序
java -jar openclaw4j-1.0-SNAPSHOT-jar-with-dependencies.jar