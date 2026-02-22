package com.openclaw.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openclaw.model.manager.LongTermMemoryManager;
import com.openclaw.model.manager.MemoryManager;
import com.openclaw.model.service.MemorySearch;

/**
 * 记忆工具管理器
 * 负责管理记忆相关的工具
 */
public class MemoryToolManager extends AbstractToolManager {

    /**
     * 构造方法
     */
    public MemoryToolManager() {
        super("memory_tool_manager", "记忆工具管理器");
    }

    /**
     * 注册默认工具
     */
    @Override
    protected void registerDefaultTools() {
        // 注册记忆读取工具
        Map<String, String> readMemoryParams = new ConcurrentHashMap<>();
        readMemoryParams.put("file_name", "记忆文件名称，如MEMORY.md、USER.md、SOUL.md");
        registerTool("read_memory", "读取指定记忆文件的内容", readMemoryParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String fileName = (String) parameters.get("file_name");
                if (fileName == null || fileName.isEmpty()) {
                    return "文件名称不能为空。";
                }
                LongTermMemoryManager memoryManager = LongTermMemoryManager.getInstance();
                return memoryManager.readLongTermMemory(fileName, true);
            } catch (Exception e) {
                return "读取记忆失败: " + e.getMessage();
            }
        });

        // 注册记忆写入工具
        Map<String, String> writeMemoryParams = new ConcurrentHashMap<>();
        writeMemoryParams.put("file_name", "记忆文件名称，如MEMORY.md、USER.md、SOUL.md");
        writeMemoryParams.put("content", "要写入的内容");
        registerTool("write_memory", "写入内容到指定记忆文件", writeMemoryParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String fileName = (String) parameters.get("file_name");
                String content = (String) parameters.get("content");
                if (fileName == null || fileName.isEmpty()) {
                    return "文件名称不能为空。";
                }
                if (content == null) {
                    return "记忆内容不能为空。";
                }
                LongTermMemoryManager memoryManager = LongTermMemoryManager.getInstance();
                boolean success = memoryManager.writeLongTermMemory(fileName, content, true);
                return success ? "记忆写入成功: " + fileName : "记忆写入失败: 未知错误";
            } catch (Exception e) {
                return "写入记忆失败: " + e.getMessage();
            }
        });

        // 注册记忆搜索工具
        Map<String, String> searchMemoryParams = new ConcurrentHashMap<>();
        searchMemoryParams.put("query", "搜索关键词");
        registerTool("search_memory", "查询记忆系统中的信息", searchMemoryParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String query = (String) parameters.get("query");
                if (query == null || query.isEmpty()) {
                    return "搜索关键词不能为空。";
                }
                MemoryManager memoryManager = MemoryManager.getInstance();
                java.util.List<MemorySearch.SearchResult> results = memoryManager.searchMemory(query, 5);
                
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
                return "查询记忆失败: " + e.getMessage();
            }
        });

        System.out.println("MemoryToolManager初始化完成，注册了 " + toolRegistry.size() + " 个工具");
    }
}
