# OpenClaw 应用程序使用说明

## 简介
OpenClaw 是一个基于 Java 开发的智能助手应用程序，它可以帮助您完成各种任务，如文件管理、网络操作、系统管理等。

## 系统要求
- Java Runtime Environment (JRE) 1.8 或更高版本
- Windows、Linux 或 macOS 操作系统

## 安装和运行

### 方法一：使用启动脚本
1. 解压 `openclaw4j-1.0-SNAPSHOT-distribution.zip` 文件到任意目录
2. 根据您的操作系统选择相应的启动脚本：
   - **Windows 系统**：双击 `start.bat` 文件
   - **Linux/macOS 系统**：在终端中运行 `chmod +x start.sh && ./start.sh`

### 方法二：手动运行
1. 解压 `openclaw4j-1.0-SNAPSHOT-distribution.zip` 文件到任意目录
2. 打开终端或命令提示符，进入解压后的目录
3. 运行以下命令：
   ```
   java -jar openclaw4j-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

## 目录结构
```
openclaw/
├── openclaw4j-1.0-SNAPSHOT-jar-with-dependencies.jar  # 主程序文件（包含所有依赖项）
├── start.bat                      # Windows 启动脚本
├── start.sh                       # Linux/macOS 启动脚本
├── resources/                     # 资源文件夹
│   ├── application.yml            # 配置文件
│   └── workflows/                 # 工作流手册
└── memory/                        # 内存数据文件夹（运行时自动创建数据）
```

## 配置
应用程序的配置信息存储在 `resources/application.yml` 文件中，您可以根据需要修改这些配置。

## 工作流
OpenClaw 支持多种工作流，用于处理不同类型的任务。工作流手册存储在 `resources/workflows/` 目录中。

## 故障排除
- **错误：找不到或无法加载主类** - 请确保您已安装 Java 运行时环境，并且版本为 1.8 或更高
- **错误：无法访问 jarfile** - 请确保您在正确的目录中运行启动脚本，并且 jar 文件存在
- **应用程序运行缓慢** - 请确保您的系统有足够的内存和 CPU 资源

## 联系信息
如果您在使用过程中遇到任何问题，请随时联系我们。