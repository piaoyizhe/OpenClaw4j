# OpenClaw4j - 智能数字员工系统

## 项目简介

OpenClaw4j 是一个基于 Java 1.8 开发的智能数字员工系统，集成了 GLM 大语言模型，具有长期记忆、智能任务规划、工具调用等功能，可以作为个人数字员工使用。

## 核心亮点

### 1. 智能记忆管理系统
- **LLM智能提炼**：使用大模型自动提取对话中的关键信息
- **自动分类判断**：智能判断信息应该写入哪个记忆文件（SOUL/USER/MEMORY/IDENTITY）
- **增量更新判断**：判断内容是否有新信息需要更新，避免重复写入
- **记忆压缩机制**：自动压缩长期记忆，保留原始内容用于追溯
- **按需加载**：启动时仅加载基础数据，完整记忆按需加载

### 2. 智能任务规划系统
- **任务分解**：使用大模型分析用户请求，自动分解为结构化任务列表
- **任务执行**：支持任务队列管理、状态跟踪、错误处理和重试逻辑
- **任务评估**：每个任务完成后，大模型会评估结果并决定后续操作
- **动态调整**：支持任务顺序调整、任务移除和新增任务
- **工具调用**：自动调用合适的工具完成任务

### 3. 系统工具框架
- **可扩展的工具系统**：支持自定义工具注册和调用
- **丰富的内置工具**：包括Shell命令执行、HTTP请求、网络信息获取、天气查询、文件操作、记忆操作等
- **统一的工具接口**：标准化的工具调用流程和参数处理
- **工具管理**：通过ToolManagerRegistry和具体的工具管理器管理工具
- **工具分类**：按功能分类的工具管理器，如FileToolManager、SystemToolManager、MemoryToolManager等

### 4. 聊天历史记录
- **完整的对话历史**：记录用户查询和系统响应
- **上下文感知**：任务生成和评估考虑整个对话的上下文
- **任务结果融入**：任务执行结果会被记录到聊天历史中

### 5. 经验值累积机制
- **自动跟踪事件**：自动跟踪特定事件发生次数
- **阈值触发**：达到阈值自动保存到长期记忆
- **经验值报告**：生成经验值报告，反映系统使用情况

## 项目结构

```
openclaw4j/
├── src/main/java/com/openclaw/
│   ├── Main.java              # 程序入口
│   ├── brain/                 # 大脑模块
│   │   ├── Hippocampus.java   # 海马体（记忆处理）
│   │   ├── LanguageProcessor.java # 语言处理器
│   │   └── Prefrontal.java    # 前额叶（任务规划）
│   ├── config/
│   │   └── ConfigManager.java # 配置管理器
│   ├── console/
│   │   └── ConsoleInterface.java # 控制台交互
│   ├── model/
│   │   ├── entity/            # 实体类
│   │   │   ├── Conversation.java  # 对话类
│   │   │   └── Message.java        # 消息类
│   │   ├── manager/            # 管理器
│   │   │   ├── DailyLogManager.java # 每日日志
│   │   │   ├── InitializationManager.java # 初始化管理
│   │   │   ├── LongTermMemoryManager.java # 长期记忆
│   │   │   ├── MemoryManager.java # 记忆管理器
│   │   │   └── SQLiteManager.java # 数据库管理
│   │   ├── service/            # 服务类
│   │   │   ├── ContentExtractor.java # 内容提炼
│   │   │   ├── ExperienceManager.java # 经验值管理
│   │   │   ├── FileIndexer.java   # 文件索引
│   │   │   └── MemorySearch.java  # 记忆搜索
│   │   └── util/               # 工具类
│   │       ├── DatabaseHealthChecker.java # 数据库健康检查
│   │       ├── MemoryCompressionManager.java # 记忆压缩
│   │       ├── OpenClawException.java # 异常类
│   │       └── ProjectManager.java # 项目管理
│   ├── test/                  # 测试类
│   ├── tools/                 # 工具模块
│   │   ├── AbstractToolManager.java # 工具管理器抽象类
│   │   ├── FileToolManager.java # 文件工具管理器
│   │   ├── HttpTool.java      # HTTP工具
│   │   ├── MemoryToolManager.java # 记忆工具管理器
│   │   ├── NetworkToolManager.java # 网络工具管理器
│   │   ├── ShellTool.java     # Shell工具
│   │   ├── SystemTool.java     # 工具接口
│   │   ├── SystemToolManager.java # 系统工具管理器
│   │   ├── ToolInfo.java      # 工具信息类
│   │   ├── ToolManager.java   # 工具管理器接口
│   │   ├── ToolManagerRegistry.java # 工具管理器注册表
│   │   ├── ToolParameters.java # 工具参数
│   │   ├── ToolResult.java    # 工具结果
│   │   ├── WeatherTool.java    # 天气工具
│   │   └── WeatherToolManager.java # 天气工具管理器
│   └── utils/                 # 工具类
│       └── LLMClient.java     # 大模型客户端
├── memory/                    # 记忆存储目录
│   ├── MEMORY.md             # 重要记忆
│   ├── USER.md               # 用户画像
│   ├── SOUL.md               # AI人格
│   ├── IDENTITY.md           # 身份信息
│   ├── TOOLS.JSON            # 工具配置
│   ├── conversation_*.md     # 对话记录
│   ├── experience.json       # 经验值数据
│   ├── openclaw.db            # SQLite数据库
│   └── *.md                   # 每日日志
├── src/main/resources/
│   └── application.yml       # 配置文件
├── pom.xml                    # Maven配置
└── README.md                  # 本文件
```

## 快速开始

### 前置要求
- Java 1.8
- Maven 3.6+
- 智谱AI API Key

### 配置

修改 `src/main/resources/application.yml`：

```yaml
openclaw:
  llm:
    provider: glm
    model: glm-4
    api_key: 你的API密钥
    api_url: https://open.bigmodel.cn/api/paas/v4/chat/completions
  memory:
    storage:
      directory: memory
    sqlite:
      db_path: memory/openclaw.db
    search:
      vector_weight: 0.7
      text_weight: 0.3
      default_max_results: 5
      default_min_score: 0.3
    daily_log:
      archive_days: 30
      clean_days: 90
  console:
    welcome_message: "欢迎使用OpenClaw记忆模式！输入您的问题，或输入 'help' 查看可用命令，输入 'exit' 退出程序。"
    prompt: "OpenClaw> "
    error_message: "抱歉，处理您的请求时发生错误。"
  tools:
    weather:
      api_key: 你的天气API密钥
```

### 编译运行

```bash
# 编译
mvn compile

# 运行
mvn exec:java -Dexec.mainClass="com.openclaw.Main"

# 运行测试
mvn test
```

## 使用说明

### 基本对话
```
OpenClaw> 你好，我叫张三，是一名Java开发工程师
```

### 命令列表

| 命令 | 说明 |
|------|------|
| `help` | 显示帮助信息 |
| `search <关键词>` | 搜索记忆中的内容 |
| `log` | 查看今日日志 |
| `log <日期>` | 查看指定日期的日志 (格式: YYYY-MM-DD) |
| `memory index` | 索引所有记忆文件 |
| `memory optimize` | 优化数据库 |
| `memory clean <天数>` | 清理指定天数前的过期数据 |
| `memory archive <天数>` | 归档指定天数前的日志 |
| `clear` | 清空控制台 |
| `new` | 开始新的对话 |
| `exit` | 退出程序 |

## 系统工作流程

### 1. 初始化流程
1. 自动创建必要的文件和目录
2. 检查用户信息是否存在，存在则跳过引导输入
3. 优化默认内容，确保系统正常运行

### 2. 对话处理流程
1. 用户输入查询
2. 系统记录到聊天历史
3. 大模型生成结构化任务列表
4. 系统执行任务并评估结果
5. 大模型根据任务结果决定后续操作
6. 系统生成最终回答并记录到聊天历史

### 3. 任务执行流程
1. 生成结构化任务列表
2. 逐个执行任务
3. 每个任务完成后，大模型评估结果
4. 根据评估结果，系统可能：
   - 继续执行剩余任务
   - 调整任务顺序
   - 移除部分任务
   - 添加新任务
   - 停止执行后续任务

## 技术栈

- Java 1.8
- Maven 3.6+
- GLM 大语言模型
- SQLite
- OkHttp3
- JSON
- SnakeYAML

## 贡献规范

### 提交代码
1. Fork 项目仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

### 代码规范
- 遵循 Java 代码规范
- 类名使用驼峰命名法（首字母大写）
- 方法名和变量名使用驼峰命名法（首字母小写）
- 常量使用全大写字母，单词间用下划线分隔
- 代码缩进使用4个空格
- 方法长度控制在合理范围内，避免过长方法
- 提供清晰的注释，特别是复杂逻辑

### 问题报告
- 使用 GitHub Issues 报告问题
- 提供详细的问题描述
- 包括复现步骤
- 提供错误信息和日志

## 许可证

MIT License