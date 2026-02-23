package com.openclaw.tools;

import com.openclaw.model.entity.ToolInfo;
import com.openclaw.model.entity.ToolParameters;
import com.openclaw.model.entity.ToolResult;
import com.openclaw.utils.WeatherUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 天气工具管理器
 * 负责管理天气相关的工具
 */
public class WeatherToolManager extends AbstractToolManager {

    /**
     * 构造方法
     */
    public WeatherToolManager() {
        super("weather_tool_manager", "天气工具管理器");
    }

    /**
     * 注册默认工具
     */
    @Override
    protected void registerDefaultTools() {
        // 注册天气查询工具
        Map<String, String> weatherParams = new ConcurrentHashMap<>();
        weatherParams.put("city", "城市名称");
        registerTool("get_weather", "获取指定城市的天气信息", weatherParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String city = (String) parameters.get("city");
                if (city == null || city.isEmpty()) {
                    return "城市名称不能为空。";
                }
                return getWeather(city);
            } catch (Exception e) {
                return "获取天气信息失败: " + e.getMessage();
            }
        });

        System.out.println("WeatherToolManager初始化完成，注册了 " + toolRegistry.size() + " 个工具");
    }

    /**
     * 获取天气信息
     * @param city 城市
     * @return 天气信息
     */
    private String getWeather(String city) throws Exception {
        // 使用WeatherUtil获取天气信息
        WeatherUtil weatherUtil = WeatherUtil.getInstance();
        ToolParameters params = new ToolParameters();
        params.setParameter("city", city);
        ToolResult result = weatherUtil.execute(params);
        // 返回具体的天气信息，而不仅仅是成功消息
        if (result.getData() != null) {
            return result.getData().toString();
        } else {
            return result.getMessage();
        }
    }
}
