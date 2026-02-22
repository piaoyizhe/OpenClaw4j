package com.openclaw.tools;

import com.openclaw.model.entity.ToolParameters;
import com.openclaw.model.entity.ToolResult;

/**
 * 系统工具接口
 * 所有系统工具都需要实现此接口
 */
public interface SystemTool {
    /**
     * 获取工具名称
     * @return 工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     * @return 工具描述
     */
    String getDescription();
    
    /**
     * 执行工具
     * @param parameters 参数
     * @return 执行结果
     */
    ToolResult execute(ToolParameters parameters);
    
    /**
     * 检查工具是否可用
     * @return 是否可用
     */
    boolean isAvailable();
}
