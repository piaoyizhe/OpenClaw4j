package com.openclaw.brain;


import com.openclaw.tools.ToolManagerRegistry;
import com.openclaw.model.manager.MemoryManager;
import com.openclaw.model.service.MemorySearch;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 任务执行器
 * 负责执行各种类型的任务
 */
public class TaskExecutor {
    private MemoryManager memoryManager;
    private ToolManagerRegistry toolManagerRegistry;
    private Map<String, Function<Prefrontal.Task, String>> taskHandlers;



    /**
     * 任务执行器构造方法
     * @param memoryManager 内存管理器
     */
    public TaskExecutor(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
        this.toolManagerRegistry = ToolManagerRegistry.getInstance();
        initializeTaskHandlers();
    }

    /**
     * 初始化任务处理器
     */
    private void initializeTaskHandlers() {
        taskHandlers = new ConcurrentHashMap<>();
        
        // 注册任务处理器
        taskHandlers.put("search_memory", this::executeSearchMemoryTask);
    }

    /**
     * 执行任务
     * @param task 任务
     * @return 执行结果
     */
    public String executeTask(Prefrontal.Task task) {
//        System.out.println("执行任务: " + task.getTaskId() + " - " + task.getTaskType());
        task.setStatus(Prefrontal.TaskStatus.RUNNING);

        try {
            String result = "";

            // 使用Map查找任务处理器，替代switch语句
            Function<Prefrontal.Task, String> handler = taskHandlers.get(task.getTaskType());
            if (handler != null) {
                // 执行对应的任务处理器
                result = handler.apply(task);
            } else {
                // 使用ToolManagerRegistry调用工具
                result = toolManagerRegistry.callTool(task.getTaskType(), task.getParameters());
            }

            task.setResult(result);
            task.setStatus(Prefrontal.TaskStatus.COMPLETED);
//            System.out.println("任务完成: " + task.getTaskId());
            return result;
        } catch (Exception e) {
            System.err.println("执行任务时发生错误: " + e.getMessage());
            String errorResult = "执行错误: " + e.getMessage();
            task.setResult(errorResult);
            task.setStatus(Prefrontal.TaskStatus.FAILED);
            System.out.println("任务失败: " + task.getTaskId());
            return errorResult;
        }
    }

    /**
     * 执行查询记忆任务
     * @param task 任务
     * @return 执行结果
     */
    private String executeSearchMemoryTask(Prefrontal.Task task) {
        try {
            String query = (String) task.getParameters().get("query");

            // 输入验证
            if (query == null || query.trim().isEmpty()) {
                return "查询内容不能为空。";
            }

            List<MemorySearch.SearchResult> results = memoryManager.searchMemory(query, 5);

            StringBuilder resultBuilder = new StringBuilder();
            if (!results.isEmpty()) {
                resultBuilder.append("找到 " + results.size() + " 条相关记忆:\n");
                for (int i = 0; i < results.size(); i++) {
                    MemorySearch.SearchResult searchResult = results.get(i);
                    resultBuilder.append((i + 1) + ". " + searchResult.getContent() + "\n");
                }
            } else {
                resultBuilder.append("未找到相关记忆");
            }

            return resultBuilder.toString();
        } catch (Exception e) {
            return "执行查询记忆任务失败: " + e.getMessage();
        }
    }




}
