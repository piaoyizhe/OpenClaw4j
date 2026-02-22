package com.openclaw.config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ConfigManager {
    private static ConfigManager instance;
    private Map<String, Object> config;

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
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.yml")) {
            if (input == null) {
                throw new IOException("配置文件未找到");
            }
            Yaml yaml = new Yaml();
            config = yaml.load(input);
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage());
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getOpenClawConfig() {
        return (Map<String, Object>) config.get("openclaw");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMemoryConfig() {
        Map<String, Object> openclawConfig = getOpenClawConfig();
        return (Map<String, Object>) openclawConfig.get("memory");
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

    public String getMemoryDirectory() {
        Map<String, Object> memoryConfig = getMemoryConfig();
        @SuppressWarnings("unchecked")
        Map<String, Object> storageConfig = (Map<String, Object>) memoryConfig.get("storage");
        return (String) storageConfig.get("directory");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDailyLogConfig() {
        Map<String, Object> memoryConfig = getMemoryConfig();
        return (Map<String, Object>) memoryConfig.get("daily_log");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getSQLiteConfig() {
        Map<String, Object> memoryConfig = getMemoryConfig();
        return (Map<String, Object>) memoryConfig.get("sqlite");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getSearchConfig() {
        Map<String, Object> memoryConfig = getMemoryConfig();
        return (Map<String, Object>) memoryConfig.get("search");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getFileIndexerConfig() {
        Map<String, Object> openclawConfig = getOpenClawConfig();
        return (Map<String, Object>) openclawConfig.get("file_indexer");
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

    public int getArchiveDays() {
        Map<String, Object> dailyLogConfig = getDailyLogConfig();
        return (Integer) dailyLogConfig.get("archive_days");
    }

    public int getCleanDays() {
        Map<String, Object> dailyLogConfig = getDailyLogConfig();
        return (Integer) dailyLogConfig.get("clean_days");
    }

    public String getSQLiteDbPath() {
        Map<String, Object> sqliteConfig = getSQLiteConfig();
        return (String) sqliteConfig.get("db_path");
    }

    public double getVectorWeight() {
        Map<String, Object> searchConfig = getSearchConfig();
        return (Double) searchConfig.get("vector_weight");
    }

    public double getTextWeight() {
        Map<String, Object> searchConfig = getSearchConfig();
        return (Double) searchConfig.get("text_weight");
    }

    public int getDefaultMaxResults() {
        Map<String, Object> searchConfig = getSearchConfig();
        return (Integer) searchConfig.get("default_max_results");
    }

    public double getDefaultMinScore() {
        Map<String, Object> searchConfig = getSearchConfig();
        return (Double) searchConfig.get("default_min_score");
    }

    public int getChunkSize() {
        Map<String, Object> fileIndexerConfig = getFileIndexerConfig();
        return (Integer) fileIndexerConfig.get("chunk_size");
    }

    public int getOverlapSize() {
        Map<String, Object> fileIndexerConfig = getFileIndexerConfig();
        return (Integer) fileIndexerConfig.get("overlap_size");
    }

    public boolean isWatchEnabled() {
        Map<String, Object> fileIndexerConfig = getFileIndexerConfig();
        return (Boolean) fileIndexerConfig.get("watch_enabled");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getLongTermMemoryConfig() {
        Map<String, Object> openclawConfig = getOpenClawConfig();
        return (Map<String, Object>) openclawConfig.get("long_term_memory");
    }

    @SuppressWarnings("unchecked")
    public String getLongTermMemoryAnalysisPrompt() {
        Map<String, Object> longTermMemoryConfig = getLongTermMemoryConfig();
        Map<String, Object> promptConfig = (Map<String, Object>) longTermMemoryConfig.get("prompt");
        return (String) promptConfig.get("analysis");
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> getToolsConfig() {
        Map<String, Object> openclawConfig = getOpenClawConfig();
        return (Map<String, Object>) openclawConfig.get("tools");
    }
    
    public String getWeatherApiKey() {
        Map<String, Object> toolsConfig = getToolsConfig();
        if (toolsConfig != null) {
            Map<String, Object> weatherConfig = (Map<String, Object>) toolsConfig.get("weather");
            if (weatherConfig != null) {
                return (String) weatherConfig.get("api_key");
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> getLogsConfig() {
        Map<String, Object> memoryConfig = getMemoryConfig();
        return (Map<String, Object>) memoryConfig.get("logs");
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
}
