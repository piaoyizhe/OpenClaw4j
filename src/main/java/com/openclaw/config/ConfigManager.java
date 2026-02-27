package com.openclaw.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

public class ConfigManager {
    private static ConfigManager instance;
    private Map<String, Object> config;
    private static final String DEFAULT_CONFIG_PATH = "src/main/resources/application.yml";
    private static final String EXTERNAL_CONFIG_PATH = "config/application.yml";
    private static final String USER_CONFIG_PATH = System.getProperty("user.home") + "/.config/openclaw/application.yml";

    private ConfigManager() {
        loadConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    private void loadConfig() {
        try {
            // 优先级：用户目录配置 > 外部配置 > 默认配置
            String configPath = getValidConfigPath();
            try (InputStream input = new FileInputStream(configPath)) {
                Yaml yaml = new Yaml();
                config = yaml.load(input);
            } catch (IOException e) {
                // 如果外部配置文件不存在，使用默认配置
                try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.yml")) {
                    if (input == null) {
                        throw new IOException("配置文件未找到");
                    }
                    Yaml yaml = new Yaml();
                    config = yaml.load(input);
                }
            }
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * 获取有效的配置文件路径
     * @return 配置文件路径
     */
    private String getValidConfigPath() {
        // 检查用户目录配置
        File userConfig = new File(USER_CONFIG_PATH);
        if (userConfig.exists()) {
            return USER_CONFIG_PATH;
        }
        
        // 检查外部配置
        File externalConfig = new File(EXTERNAL_CONFIG_PATH);
        if (externalConfig.exists()) {
            return EXTERNAL_CONFIG_PATH;
        }
        
        // 使用默认配置
        return DEFAULT_CONFIG_PATH;
    }
    
    /**
     * 确保配置目录存在
     */
    private void ensureConfigDirectories() {
        // 确保外部配置目录存在
        File externalConfigDir = new File(EXTERNAL_CONFIG_PATH).getParentFile();
        if (externalConfigDir != null && !externalConfigDir.exists()) {
            externalConfigDir.mkdirs();
        }
        
        // 确保用户配置目录存在
        File userConfigDir = new File(USER_CONFIG_PATH).getParentFile();
        if (userConfigDir != null && !userConfigDir.exists()) {
            userConfigDir.mkdirs();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getOpenClawConfig() {
        return (Map<String, Object>) config.get("openclaw");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getLLMConfig() {
        Map<String, Object> openclawConfig = getOpenClawConfig();
        return (Map<String, Object>) openclawConfig.get("llm");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getConsoleConfig() {
        Map<String, Object> openclawConfig = getOpenClawConfig();
        return (Map<String, Object>) openclawConfig.get("console");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMemoryConfig() {
        Map<String, Object> openclawConfig = getOpenClawConfig();
        return (Map<String, Object>) openclawConfig.get("memory");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getLogsConfig() {
        Map<String, Object> memoryConfig = getMemoryConfig();
        return (Map<String, Object>) memoryConfig.get("logs");
    }

    public String getLLMApiKey() {
        Map<String, Object> llmConfig = getLLMConfig();
        return (String) llmConfig.get("api_key");
    }

    public String getLLMApiUrl() {
        Map<String, Object> llmConfig = getLLMConfig();
        return (String) llmConfig.get("api_url");
    }

    public String getLLMModel() {
        Map<String, Object> llmConfig = getLLMConfig();
        return (String) llmConfig.get("model");
    }

    public String getLLMVisionModel() {
        Map<String, Object> llmConfig = getLLMConfig();
        return (String) llmConfig.getOrDefault("vision_model", "glm-4.6v");
    }

    public String getMemoryDirectory() {
        Map<String, Object> memoryConfig = getMemoryConfig();
        @SuppressWarnings("unchecked")
        Map<String, Object> storageConfig = (Map<String, Object>) memoryConfig.get("storage");
        return (String) storageConfig.get("directory");
    }

    public String getWelcomeMessage() {
        Map<String, Object> consoleConfig = getConsoleConfig();
        return (String) consoleConfig.get("welcome_message");
    }

    public String getPrompt() {
        Map<String, Object> consoleConfig = getConsoleConfig();
        return (String) consoleConfig.get("prompt");
    }

    public String getErrorMessage() {
        Map<String, Object> consoleConfig = getConsoleConfig();
        return (String) consoleConfig.get("error_message");
    }

    public String getDailyLogCronExpression() {
        Map<String, Object> logsConfig = getLogsConfig();
        if (logsConfig != null) {
            Map<String, Object> dailyLogConfig = (Map<String, Object>) logsConfig.get("daily_log");
            if (dailyLogConfig != null) {
                return (String) dailyLogConfig.get("cron_expression");
            }
        }
        return "0 0 22 * * ?"; // 默认每天22:00执行
    }

    public String getWeeklyReportCronExpression() {
        Map<String, Object> logsConfig = getLogsConfig();
        if (logsConfig != null) {
            Map<String, Object> weeklyConfig = (Map<String, Object>) logsConfig.get("weekly_report");
            if (weeklyConfig != null) {
                return (String) weeklyConfig.get("cron_expression");
            }
        }
        return "0 0 22 ? * 6"; // 默认每周六22:00执行
    }

    public String getMonthlyReportCronExpression() {
        Map<String, Object> logsConfig = getLogsConfig();
        if (logsConfig != null) {
            Map<String, Object> monthlyConfig = (Map<String, Object>) logsConfig.get("monthly_report");
            if (monthlyConfig != null) {
                return (String) monthlyConfig.get("cron_expression");
            }
        }
        return "0 0 22 1 * ?"; // 默认每月1日22:00执行
    }

    public boolean isDailyLogEnabled() {
        Map<String, Object> logsConfig = getLogsConfig();
        if (logsConfig != null) {
            Map<String, Object> dailyLogConfig = (Map<String, Object>) logsConfig.get("daily_log");
            if (dailyLogConfig != null) {
                return (Boolean) dailyLogConfig.getOrDefault("enabled", true);
            }
        }
        return true;
    }

    public boolean isWeeklyReportEnabled() {
        Map<String, Object> logsConfig = getLogsConfig();
        if (logsConfig != null) {
            Map<String, Object> weeklyConfig = (Map<String, Object>) logsConfig.get("weekly_report");
            if (weeklyConfig != null) {
                return (Boolean) weeklyConfig.getOrDefault("enabled", true);
            }
        }
        return true;
    }

    public boolean isMonthlyReportEnabled() {
        Map<String, Object> logsConfig = getLogsConfig();
        if (logsConfig != null) {
            Map<String, Object> monthlyConfig = (Map<String, Object>) logsConfig.get("monthly_report");
            if (monthlyConfig != null) {
                return (Boolean) monthlyConfig.getOrDefault("enabled", true);
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMailConfig() {
        Map<String, Object> openclawConfig = getOpenClawConfig();
        return (Map<String, Object>) openclawConfig.get("mail");
    }

    public boolean isMailEnabled() {
        Map<String, Object> mailConfig = getMailConfig();
        if (mailConfig != null) {
            return (Boolean) mailConfig.getOrDefault("enabled", false);
        }
        return false;
    }

    public String getMailHost() {
        Map<String, Object> mailConfig = getMailConfig();
        if (mailConfig != null) {
            return (String) mailConfig.get("host");
        }
        return "smtp.qq.com";
    }

    public int getMailPort() {
        Map<String, Object> mailConfig = getMailConfig();
        if (mailConfig != null) {
            return (Integer) mailConfig.get("port");
        }
        return 465;
    }

    public String getMailUsername() {
        Map<String, Object> mailConfig = getMailConfig();
        if (mailConfig != null) {
            return (String) mailConfig.get("username");
        }
        return "";
    }

    public String getMailPassword() {
        Map<String, Object> mailConfig = getMailConfig();
        if (mailConfig != null) {
            return (String) mailConfig.get("password");
        }
        return "";
    }

    public String getMailFrom() {
        // 直接返回username作为from
        return getMailUsername();
    }
    
    public String getMailSenderName() {
        Map<String, Object> mailConfig = getMailConfig();
        if (mailConfig != null) {
            return (String) mailConfig.getOrDefault("sender_name", "");
        }
        return "";
    }

    public boolean isMailSsl() {
        Map<String, Object> mailConfig = getMailConfig();
        if (mailConfig != null) {
            return (Boolean) mailConfig.getOrDefault("ssl", true);
        }
        return true;
    }

    public int getMailTimeout() {
        Map<String, Object> mailConfig = getMailConfig();
        if (mailConfig != null) {
            return (Integer) mailConfig.getOrDefault("timeout", 30000);
        }
        return 30000;
    }
    
    /**
     * 保存配置到文件
     * @return 是否保存成功
     */
    public boolean saveConfig() {
        try {
            // 确保配置目录存在
            ensureConfigDirectories();
            
            // 保存到外部配置目录
            try (OutputStream output = new FileOutputStream(EXTERNAL_CONFIG_PATH);
                 OutputStreamWriter writer = new OutputStreamWriter(output)) {
                Yaml yaml = new Yaml();
                yaml.dump(config, writer);
            }
            
            // 重新加载配置，确保内存中的配置与文件一致
            loadConfig();
            
            return true;
        } catch (IOException e) {
            System.err.println("保存配置文件失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新大模型配置
     * @param apiKey API密钥
     * @param apiUrl API地址
     * @param model 模型名称
     */
    public void updateLLMConfig(String apiKey, String apiUrl, String model) {
        Map<String, Object> llmConfig = getLLMConfig();
        if (apiKey != null && !apiKey.isEmpty()) {
            llmConfig.put("api_key", apiKey);
        }
        if (apiUrl != null && !apiUrl.isEmpty()) {
            llmConfig.put("api_url", apiUrl);
        }
        if (model != null && !model.isEmpty()) {
            llmConfig.put("model", model);
        }
    }
    
    /**
     * 更新邮箱配置
     * @param host 邮件服务器主机
     * @param port 邮件服务器端口
     * @param username 用户名
     * @param password 密码
     * @param senderName 发件人姓名
     */
    public void updateMailConfig(String host, int port, String username, String password, String senderName) {
        Map<String, Object> mailConfig = getMailConfig();
        if (host != null && !host.isEmpty()) {
            mailConfig.put("host", host);
        }
        if (port > 0) {
            mailConfig.put("port", port);
        }
        if (username != null && !username.isEmpty()) {
            mailConfig.put("username", username);
        }
        if (password != null && !password.isEmpty()) {
            mailConfig.put("password", password);
        }
        if (senderName != null && !senderName.isEmpty()) {
            mailConfig.put("sender_name", senderName);
        }
        mailConfig.put("enabled", true);
    }
    
    /**
     * 更新数字员工信息
     * @param name 姓名
     * @param description 功能简介
     */
    public void updateDigitalEmployeeInfo(String name, String description) {
        // 数字员工信息存储在IDENTITY.md文件中，由LongTermMemoryManager管理
    }
}
