# 工具系统（Tools）

## 文件夹作用

`tools` 文件夹是 OpenClaw4j 智能数字员工系统的工具模块，负责管理和执行各种系统工具。该模块提供了一个可扩展的工具框架，支持自定义工具的注册和调用，使系统能够执行各种任务，如文件操作、网络请求、系统信息获取等。

## 核心组件

### 1. 工具管理器

- **ToolManager.java**：工具管理器接口，定义了工具管理器的通用方法
- **AbstractToolManager.java**：工具管理器抽象类，实现了ToolManager接口的通用方法
- **ToolManagerRegistry.java**：工具管理器注册表，负责管理所有工具管理器，实现工具的动态路由

### 2. 具体工具管理器

- **FileToolManager.java**：文件工具管理器，负责文件相关的工具，如读取、写入、创建、删除文件等
- **SystemToolManager.java**：系统工具管理器，负责系统相关的工具，如获取本地IP、系统信息等
- **MemoryToolManager.java**：记忆工具管理器，负责记忆相关的工具，如读取、写入、搜索记忆等
- **NetworkToolManager.java**：网络工具管理器，负责网络相关的工具，如发送HTTP请求、执行Shell命令等
- **WeatherToolManager.java**：天气工具管理器，负责天气相关的工具，如获取天气信息等

### 3. 工具实现

- **HttpTool.java**：HTTP工具，用于发送HTTP请求
- **ShellTool.java**：Shell工具，用于执行Shell命令
- **WeatherTool.java**：天气工具，用于获取天气信息

### 4. 工具相关类

- **ToolInfo.java**：工具信息类，存储工具的基本信息和调用接口
- **ToolParameters.java**：工具参数类，用于传递工具参数
- **ToolResult.java**：工具结果类，用于返回工具执行结果
- **SystemTool.java**：工具接口，定义了工具的执行方法

## 如何添加新工具

### 步骤1：创建工具类（可选）

如果需要实现一个新的工具，可以创建一个实现了`SystemTool`接口的工具类。例如：

```java
public class MyTool implements SystemTool {
    private static MyTool instance;

    private MyTool() {
    }

    public static MyTool getInstance() {
        if (instance == null) {
            synchronized (MyTool.class) {
                if (instance == null) {
                    instance = new MyTool();
                }
            }
        }
        return instance;
    }

    @Override
    public ToolResult execute(ToolParameters parameters) {
        try {
            // 获取参数
            String param1 = parameters.getParameter("param1");
            String param2 = parameters.getParameter("param2");
            
            // 实现工具逻辑
            String result = "执行结果: " + param1 + ", " + param2;
            
            return ToolResult.success("执行成功", result);
        } catch (Exception e) {
            return ToolResult.failure("执行失败: " + e.getMessage());
        }
    }
}
```

### 步骤2：注册工具到工具管理器

有两种方式注册工具：

#### 方式A：使用现有工具管理器

选择一个合适的现有工具管理器，在其`registerDefaultTools`方法中注册新工具。例如，在`SystemToolManager.java`中添加：

```java
// 注册我的工具
Map<String, String> myToolParams = new ConcurrentHashMap<>();
myToolParams.put("param1", "参数1描述");
myToolParams.put("param2", "参数2描述");
registerTool("my_tool", "我的自定义工具", myToolParams, (ToolInfo.ToolCaller) parameters -> {
    try {
        MyTool myTool = MyTool.getInstance();
        ToolParameters params = new ToolParameters(parameters);
        ToolResult result = myTool.execute(params);
        return result.getMessage();
    } catch (Exception e) {
        return "执行工具失败: " + e.getMessage();
    }
});
```

#### 方式B：创建新的工具管理器

如果新工具属于一个新的功能领域，可以创建一个新的工具管理器：

1. 创建一个继承自`AbstractToolManager`的工具管理器类：

```java
public class MyToolManager extends AbstractToolManager {
    public MyToolManager() {
        super("my_tool_manager", "我的工具管理器");
    }

    @Override
    protected void registerDefaultTools() {
        // 注册工具
        Map<String, String> myToolParams = new ConcurrentHashMap<>();
        myToolParams.put("param1", "参数1描述");
        myToolParams.put("param2", "参数2描述");
        registerTool("my_tool", "我的自定义工具", myToolParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                // 实现工具逻辑
                String param1 = (String) parameters.get("param1");
                String param2 = (String) parameters.get("param2");
                return "执行结果: " + param1 + ", " + param2;
            } catch (Exception e) {
                return "执行工具失败: " + e.getMessage();
            }
        });

        System.out.println("MyToolManager初始化完成，注册了 " + toolRegistry.size() + " 个工具");
    }
}
```

2. 在`ToolManagerRegistry.java`的`initialize`方法中注册新的工具管理器：

```java
private void initialize() {
    // 注册默认的工具管理器
    registerToolManager(new FileToolManager());
    registerToolManager(new SystemToolManager());
    registerToolManager(new MemoryToolManager());
    registerToolManager(new NetworkToolManager());
    registerToolManager(new WeatherToolManager());
    registerToolManager(new MyToolManager()); // 添加这一行
}
```

### 步骤3：验证工具注册

启动系统后，查看控制台输出，确认工具管理器注册成功：

```
工具管理器注册成功: my_tool_manager
MyToolManager初始化完成，注册了 1 个工具
```

### 步骤4：使用工具

系统会自动将所有注册的工具提供给大模型，大模型会根据用户的请求选择合适的工具来执行任务。

## 工具调用流程

1. 用户发送请求
2. 大模型分析请求，决定是否需要调用工具
3. 如果需要调用工具，大模型会生成工具调用请求，包含工具名称和参数
4. 系统通过`ToolManagerRegistry`找到对应的工具管理器
5. 工具管理器执行工具，并返回执行结果
6. 系统将执行结果发送给大模型
7. 大模型根据执行结果生成最终回答

## 工具参数格式

工具参数使用JSON格式，例如：

```json
{
  "param1": "参数值1",
  "param2": "参数值2"
}
```

## 最佳实践

1. **工具命名**：工具名称应使用小写字母和下划线，简洁明了
2. **参数描述**：为每个参数提供清晰的描述，帮助大模型正确使用工具
3. **错误处理**：工具应妥善处理异常，返回有意义的错误信息
4. **安全性**：工具应进行适当的输入验证，避免安全风险
5. **性能**：工具应尽量高效，避免长时间阻塞

## 示例：添加一个计算器工具

### 1. 创建工具类

```java
public class CalculatorTool implements SystemTool {
    private static CalculatorTool instance;

    private CalculatorTool() {
    }

    public static CalculatorTool getInstance() {
        if (instance == null) {
            synchronized (CalculatorTool.class) {
                if (instance == null) {
                    instance = new CalculatorTool();
                }
            }
        }
        return instance;
    }

    @Override
    public ToolResult execute(ToolParameters parameters) {
        try {
            String operation = parameters.getParameter("operation");
            double num1 = Double.parseDouble(parameters.getParameter("num1"));
            double num2 = Double.parseDouble(parameters.getParameter("num2"));
            
            double result = 0;
            switch (operation) {
                case "add":
                    result = num1 + num2;
                    break;
                case "subtract":
                    result = num1 - num2;
                    break;
                case "multiply":
                    result = num1 * num2;
                    break;
                case "divide":
                    if (num2 == 0) {
                        return ToolResult.failure("除数不能为0");
                    }
                    result = num1 / num2;
                    break;
                default:
                    return ToolResult.failure("不支持的操作: " + operation);
            }
            
            return ToolResult.success("计算成功", String.valueOf(result));
        } catch (Exception e) {
            return ToolResult.failure("计算失败: " + e.getMessage());
        }
    }
}
```

### 2. 注册工具

在`SystemToolManager.java`的`registerDefaultTools`方法中添加：

```java
// 注册计算器工具
Map<String, String> calculatorParams = new ConcurrentHashMap<>();
calculatorParams.put("operation", "操作类型: add(加法), subtract(减法), multiply(乘法), divide(除法)");
calculatorParams.put("num1", "第一个数字");
calculatorParams.put("num2", "第二个数字");
registerTool("calculate", "执行数学计算", calculatorParams, (ToolInfo.ToolCaller) parameters -> {
    try {
        CalculatorTool calculatorTool = CalculatorTool.getInstance();
        ToolParameters params = new ToolParameters(parameters);
        ToolResult result = calculatorTool.execute(params);
        return result.getMessage();
    } catch (Exception e) {
        return "执行计算失败: " + e.getMessage();
    }
});
```

### 3. 使用工具

用户可以这样使用计算器工具：

```
OpenClaw> 计算 123 + 456
```

系统会调用`calculate`工具，执行加法操作，并返回结果。

## 总结

工具系统是OpenClaw4j的一个重要组成部分，它使系统能够执行各种任务，扩展了系统的功能。通过本文档的指导，您可以轻松地添加自定义工具，使系统更加强大和灵活。