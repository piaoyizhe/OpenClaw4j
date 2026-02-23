package com.openclaw.utils;

import com.openclaw.model.entity.ToolParameters;
import com.openclaw.model.entity.ToolResult;

/**
 * 天气查询工具
 * （示例实现，需要配置真实的天气API）
 */
public class WeatherUtil {
    private static WeatherUtil instance;

    /**
     * 私有构造方法
     */
    private WeatherUtil() {
    }

    /**
     * 获取单例实例
     * @return WeatherUtil实例
     */
    public static WeatherUtil getInstance() {
        if (instance == null) {
            synchronized (WeatherUtil.class) {
                if (instance == null) {
                    instance = new WeatherUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 执行天气查询
     * @param parameters 参数
     * @return 执行结果
     */
    public ToolResult execute(ToolParameters parameters) {
        long startTime = System.currentTimeMillis();
        
        try {
            String city = parameters.getString("city");
            if (city == null || city.isEmpty()) {
                city = "北京";
            }
            
            String date = parameters.getString("date");
            if (date == null || date.isEmpty()) {
                date = "今天";
            }
            
            // 这里需要集成真实的天气API
            // 暂时返回模拟数据
            String weatherInfo = getMockWeatherInfo(city, date);
            
            long executionTime = System.currentTimeMillis() - startTime;
            // 将具体的天气信息作为data，成功消息作为message
            ToolResult result = ToolResult.success("查询成功", weatherInfo);
            result.setExecutionTime(executionTime);
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            ToolResult result = ToolResult.failure("查询天气失败: " + e.getMessage());
            result.setExecutionTime(executionTime);
            return result;
        }
    }

    /**
     * 检查工具是否可用
     * @return 是否可用
     */
    public boolean isAvailable() {
        // 即使没有配置API密钥，也返回true以使用模拟数据
        return true;
    }
    
    /**
     * 获取模拟天气信息
     */
    private String getMockWeatherInfo(String city, String date) {
        return String.format(
            "【%s】%s天气情况\n" +
            "温度: 15-25°C\n" +
            "天气: 晴转多云\n" +
            "风向: 东南风 3-4级\n" +
            "湿度: 60%%\n" +
            "紫外线: 中等\n" +
            "提示: 适宜外出，建议携带薄外套",
            city, date
        );
    }
}