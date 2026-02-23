# OpenClaw4j - 智能数字员工系统

## 项目简介

OpenClaw4j 是一个基于 Java 1.8 开发的智能数字员工系统，集成了 GLM 大语言模型，具有长期记忆、智能任务规划、工具调用、工作流程管理等功能，可以作为个人数字员工使用。

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

### 4. 工作流程管理系统
- **意图识别**：使用大模型识别用户意图，选择合适的工作流程
- **工作手册**：基于Skills格式的工作流程手册，包含详细的执行步骤
- **工具过滤**：根据工作流程选择相关工具，避免加载无关工具
- **场景化处理**：针对不同场景提供标准化的处理流程
- **工作流程执行**：按照预定步骤执行工作流程，确保任务完成质量

### 5. 聊天历史记录
- **完整的对话历史**：记录用户查询和系统响应
- **上下文感知**：任务生成和评估考虑整个对话的上下文
- **任务结果融入**：任务执行结果会被记录到聊天历史中

### 6. 经验值累积机制
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
│   │   ├── Prefrontal.java    # 前额叶（任务规划）
│   │   ├── TaskExecutor.java  # 任务执行器
│   │   └── WorkflowManager.java # 工作流程管理器
│   ├── config/                # 配置模块
│   │   └── ConfigManager.java # 配置管理器
│   ├── console/               # 控制台模块
│   │   └── ConsoleInterface.java # 控制台交互
│   ├── model/                 # 模型模块
│   │   ├── entity/            # 实体类
│   │   │   ├── Conversation.java  # 对话类
│   │   │   ├── Message.java        # 消息类
│   │   │   ├── ToolInfo.java       # 工具信息类
│   │   │   ├── ToolParameters.java # 工具参数类
│   │   │   └── ToolResult.java     # 工具结果类
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
│   │       └── ProjectManager.java # 项目管理
│   ├── test/                  # 测试类
│   ├── tools/                 # 工具模块
│   │   ├── AbstractToolManager.java # 工具管理器抽象类
│   │   ├── FileToolManager.java # 文件工具管理器
│   │   ├── HttpToolManager.java # HTTP工具管理器
│   │   ├── MemoryToolManager.java # 记忆工具管理器
│   │   ├── NetworkToolManager.java # 网络工具管理器
│   │   ├── ShellToolManager.java # Shell工具管理器
│   │   ├── SystemToolManager.java # 系统工具管理器
│   │   ├── ToolManager.java   # 工具管理器接口
│   │   ├── ToolManagerRegistry.java # 工具管理器注册表
│   │   └── WeatherToolManager.java # 天气工具管理器
│   └── utils/                 # 工具类
│       ├── ApplicationContext.java # 应用上下文
│       ├── DatabaseConnectionPool.java # 数据库连接池
│       ├── DateUtils.java      # 日期工具
│       ├── DependencyInjector.java # 依赖注入
│       ├── EmailUtils.java     # 邮件工具
│       ├── ErrorHandler.java   # 错误处理器
│       ├── HttpUtil.java       # HTTP工具
│       ├── LLMClient.java      # 大模型客户端
│       ├── OpenClawException.java # 异常类
│       ├── ShellUtil.java      # Shell工具
│       ├── StringUtil.java     # 字符串工具
│       ├── WeatherUtil.java    # 天气工具
│       └── WorkUtils.java      # 工作工具
├── src/main/resources/         # 资源目录
│   ├── workflows/             # 工作流程手册
│   │   ├── file_management_manual.md # 文件管理工作手册
│   │   ├── network_operation_manual.md # 网络操作工作手册
│   │   ├── school_bidding_manual.md # 学校招标网站信息获取工作手册
│   │   ├── school_timetable_manual.md # 学校排课工作流程
│   │   └── system_management_manual.md # 系统管理工作手册
│   └── application.yml        # 配置文件
├── .gitignore                 # Git忽略文件
├── EXECUTION_FLOW_ANALYSIS.md # 执行流程分析
├── LICENSE                    # 许可证
├── README.md                  # 本文件
└── pom.xml                    # Maven配置
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
  email:
    smtp:
      host: smtp.qq.com
      port: 465
      username: 你的邮箱
      password: 你的邮箱密码
      ssl: true
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

### 工作流程示例

#### 1. 文件管理
```
OpenClaw> 帮我列出当前目录下的所有文件
```

#### 2. 网络操作
```
OpenClaw> 帮我调用天气API获取北京的天气
```

#### 3. 系统管理
```
OpenClaw> 帮我查看当前系统信息
```

#### 4. 学校排课
```
OpenClaw> 帮我为学校创建排课表
```

## 系统工作流程

### 1. 初始化流程
1. 自动创建必要的文件和目录
2. 检查用户信息是否存在，存在则跳过引导输入
3. 优化默认内容，确保系统正常运行
4. 加载工作流程手册

### 2. 对话处理流程
1. 用户输入查询
2. 系统记录到聊天历史
3. 工作流程管理器识别用户意图
4. 加载合适的工作流程手册
5. 大模型生成结构化任务列表
6. 系统执行任务并评估结果
7. 大模型根据任务结果决定后续操作
8. 系统生成最终回答并记录到聊天历史

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

### 4. 工作流程执行流程
1. 识别用户意图
2. 选择合适的工作流程
3. 加载工作流程手册
4. 提取所需工具列表
5. 执行工作流程步骤
6. 生成执行报告

## 技术栈

- Java 1.8
- Maven 3.6+
- GLM 大语言模型
- SQLite
- OkHttp3
- com.alibaba.fastjson
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

## 许可证

MIT License
