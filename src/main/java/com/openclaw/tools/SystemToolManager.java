package com.openclaw.tools;

import com.openclaw.model.entity.ToolInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统工具管理器
 * 负责管理系统相关的工具
 */
public class SystemToolManager extends AbstractToolManager {

    /**
     * 构造方法
     */
    public SystemToolManager() {
        super("system_tool_manager", "系统工具管理器");
    }

    /**
     * 注册默认工具
     */
    @Override
    protected void registerDefaultTools() {
        // 获取可用工具列表工具
        registerTool("get_tools_list", "获取可用的工具列表", new ConcurrentHashMap<>(), (ToolInfo.ToolCaller) parameters -> {
            try {
                return getToolsInfoAsJsonArray().toJSONString();
            } catch (Exception e) {
                return "获取本地IP失败: " + e.getMessage();
            }
        });

        // 注册本地IP获取工具
        Map<String, String> localIpParams = new ConcurrentHashMap<>();
        registerTool("get_local_ip", "获取本地网络信息，包括主机名和IP地址", localIpParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                return getLocalIp();
            } catch (Exception e) {
                return "获取本地IP失败: " + e.getMessage();
            }
        });

        // 注册系统信息工具
        Map<String, String> systemInfoParams = new ConcurrentHashMap<>();
        registerTool("get_system_info", "获取系统信息，包括操作系统、Java版本等", systemInfoParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                return getSystemInfo();
            } catch (Exception e) {
                return "获取系统信息失败: " + e.getMessage();
            }
        });

        // 注册邮件发送工具
        Map<String, String> emailParams = new ConcurrentHashMap<>();
        emailParams.put("to", "收件人邮箱地址");
        emailParams.put("subject", "邮件主题");
        emailParams.put("body", "邮件正文");
        emailParams.put("from", "发件人姓名（如：张三，用于显示在邮件中）");
        registerTool("send_email", "发送邮件到指定邮箱", emailParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String to = (String) parameters.get("to");
                String subject = (String) parameters.get("subject");
                String body = (String) parameters.get("body");
                String from = (String) parameters.get("from");
                return sendEmail(to, subject, body, from);
            } catch (Exception e) {
                return "发送邮件失败: " + e.getMessage();
            }
        });

        // 注册写日报工具
        Map<String, String> writeDailyParams = new ConcurrentHashMap<>();
        writeDailyParams.put("content", "日报内容");
        writeDailyParams.put("date", "日期（格式：yyyy-MM-dd），可选");
        registerTool("write_daily_report", "写日报", writeDailyParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String content = (String) parameters.get("content");
                String date = (String) parameters.get("date");
                com.openclaw.utils.WorkUtils workUtils = com.openclaw.utils.WorkUtils.getInstance();
                return workUtils.writeDailyReport(content, date);
            } catch (Exception e) {
                return "写日报失败: " + e.getMessage();
            }
        });

        // 注册读日报工具
        Map<String, String> readDailyParams = new ConcurrentHashMap<>();
        readDailyParams.put("date", "日期（格式：yyyy-MM-dd），可选");
        registerTool("read_daily_report", "读日报", readDailyParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String date = (String) parameters.get("date");
                com.openclaw.utils.WorkUtils workUtils = com.openclaw.utils.WorkUtils.getInstance();
                return workUtils.readDailyReport(date);
            } catch (Exception e) {
                return "读日报失败: " + e.getMessage();
            }
        });

        // 注册写周报工具
        Map<String, String> writeWeeklyParams = new ConcurrentHashMap<>();
        writeWeeklyParams.put("content", "周报内容");
        writeWeeklyParams.put("year", "年份，可选");
        writeWeeklyParams.put("week", "周数，可选");
        registerTool("write_weekly_report", "写周报", writeWeeklyParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String content = (String) parameters.get("content");
                String year = (String) parameters.get("year");
                String week = (String) parameters.get("week");
                com.openclaw.utils.WorkUtils workUtils = com.openclaw.utils.WorkUtils.getInstance();
                return workUtils.writeWeeklyReport(content, year, week);
            } catch (Exception e) {
                return "写周报失败: " + e.getMessage();
            }
        });

        // 注册读周报工具
        Map<String, String> readWeeklyParams = new ConcurrentHashMap<>();
        readWeeklyParams.put("year", "年份，可选");
        readWeeklyParams.put("week", "周数，可选");
        registerTool("read_weekly_report", "读周报", readWeeklyParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String year = (String) parameters.get("year");
                String week = (String) parameters.get("week");
                com.openclaw.utils.WorkUtils workUtils = com.openclaw.utils.WorkUtils.getInstance();
                return workUtils.readWeeklyReport(year, week);
            } catch (Exception e) {
                return "读周报失败: " + e.getMessage();
            }
        });

        // 注册列出日报工具
        Map<String, String> listDailyParams = new ConcurrentHashMap<>();
        registerTool("list_daily_reports", "列出所有日报", listDailyParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                com.openclaw.utils.WorkUtils workUtils = com.openclaw.utils.WorkUtils.getInstance();
                return workUtils.listDailyReports();
            } catch (Exception e) {
                return "列出日报失败: " + e.getMessage();
            }
        });

        // 注册列出周报工具
        Map<String, String> listWeeklyParams = new ConcurrentHashMap<>();
        registerTool("list_weekly_reports", "列出所有周报", listWeeklyParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                com.openclaw.utils.WorkUtils workUtils = com.openclaw.utils.WorkUtils.getInstance();
                return workUtils.listWeeklyReports();
            } catch (Exception e) {
                return "列出周报失败: " + e.getMessage();
            }
        });

        // 注册定时任务工具
        Map<String, String> scheduleTaskParams = new ConcurrentHashMap<>();
        scheduleTaskParams.put("task_type", "任务类型：once（一次性）、fixed_rate（固定频率）、fixed_delay（固定延迟）");
        scheduleTaskParams.put("initial_delay", "初始延迟（秒）");
        scheduleTaskParams.put("period", "执行周期（秒），仅对fixed_rate和fixed_delay有效");
        scheduleTaskParams.put("task_description", "任务描述");
        registerTool("schedule_task", "调度定时任务", scheduleTaskParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String taskType = (String) parameters.get("task_type");
                String initialDelayStr = (String) parameters.get("initial_delay");
                String periodStr = (String) parameters.get("period");
                String taskDescription = (String) parameters.get("task_description");

                if (taskType == null || initialDelayStr == null) {
                    return "任务类型和初始延迟不能为空。";
                }

                long initialDelay = Long.parseLong(initialDelayStr);
                com.openclaw.utils.SchedulerUtils scheduler = com.openclaw.utils.SchedulerUtils.getInstance();
                String taskId;

                switch (taskType.toLowerCase()) {
                    case "once":
                        taskId = scheduler.scheduleTask(() -> {
                            System.out.println("执行定时任务: " + taskDescription);
                        }, initialDelay, java.util.concurrent.TimeUnit.SECONDS);
                        break;
                    case "fixed_rate":
                        if (periodStr == null) {
                            return "固定频率任务需要指定执行周期。";
                        }
                        long period = Long.parseLong(periodStr);
                        taskId = scheduler.scheduleAtFixedRate(() -> {
                            System.out.println("执行固定频率任务: " + taskDescription);
                        }, initialDelay, period, java.util.concurrent.TimeUnit.SECONDS);
                        break;
                    case "fixed_delay":
                        if (periodStr == null) {
                            return "固定延迟任务需要指定执行周期。";
                        }
                        long delay = Long.parseLong(periodStr);
                        taskId = scheduler.scheduleWithFixedDelay(() -> {
                            System.out.println("执行固定延迟任务: " + taskDescription);
                        }, initialDelay, delay, java.util.concurrent.TimeUnit.SECONDS);
                        break;
                    default:
                        return "不支持的任务类型，支持：once、fixed_rate、fixed_delay。";
                }

                return "任务调度成功，任务ID: " + taskId;
            } catch (Exception e) {
                return "调度任务失败: " + e.getMessage();
            }
        });

        // 注册列出定时任务工具
        Map<String, String> listTasksParams = new ConcurrentHashMap<>();
        registerTool("list_scheduled_tasks", "列出所有定时任务", listTasksParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                com.openclaw.utils.SchedulerUtils scheduler = com.openclaw.utils.SchedulerUtils.getInstance();
                java.util.List<String> tasks = scheduler.listTasks();
                if (tasks.isEmpty()) {
                    return "当前没有定时任务。";
                }
                StringBuilder result = new StringBuilder();
                result.append("当前定时任务:\n");
                for (String taskId : tasks) {
                    result.append("- " + taskId + " (运行中: " + scheduler.isTaskRunning(taskId) + ")\n");
                }
                return result.toString();
            } catch (Exception e) {
                return "列出任务失败: " + e.getMessage();
            }
        });

        // 注册取消定时任务工具
        Map<String, String> cancelTaskParams = new ConcurrentHashMap<>();
        cancelTaskParams.put("task_id", "任务ID");
        registerTool("cancel_scheduled_task", "取消定时任务", cancelTaskParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String taskId = (String) parameters.get("task_id");
                if (taskId == null || taskId.isEmpty()) {
                    return "任务ID不能为空。";
                }
                com.openclaw.utils.SchedulerUtils scheduler = com.openclaw.utils.SchedulerUtils.getInstance();
                boolean success = scheduler.cancelTask(taskId);
                return success ? "任务取消成功。" : "任务取消失败，可能任务不存在。";
            } catch (Exception e) {
                return "取消任务失败: " + e.getMessage();
            }
        });

//        System.out.println("SystemToolManager初始化完成，注册了 " + toolRegistry.size() + " 个工具");
    }

    /**
     * 获取本地IP
     * @return 本地网络信息
     */
    private String getLocalIp() throws Exception {
        StringBuilder result = new StringBuilder();
        result.append("本地网络信息:\n");
        
        // 获取本地主机名
        java.net.InetAddress localhost = java.net.InetAddress.getLocalHost();
        result.append("主机名: " + localhost.getHostName() + "\n");
        
        // 获取本地IP地址
        result.append("IP地址: " + localhost.getHostAddress() + "\n");
        
        return result.toString();
    }

    /**
     * 获取系统信息
     * @return 系统信息
     */
    private String getSystemInfo() throws Exception {
        StringBuilder result = new StringBuilder();
        result.append("系统信息:\n");
        
        // 操作系统信息
        result.append("操作系统: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "\n");
        result.append("操作系统架构: " + System.getProperty("os.arch") + "\n");
        
        // Java信息
        result.append("Java版本: " + System.getProperty("java.version") + "\n");
        result.append("Java虚拟机: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + "\n");
        
        // 运行时信息
        result.append("最大内存: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB\n");
        result.append("可用内存: " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + " MB\n");
        
        return result.toString();
    }

    /**
     * 发送邮件
     * @param to 收件人
     * @param subject 主题
     * @param body 正文
     * @param from 发件人姓名（用于显示）
     * @return 发送结果
     */
    private String sendEmail(String to, String subject, String body, String from) throws Exception {
        com.openclaw.utils.EmailUtils emailUtils = com.openclaw.utils.EmailUtils.getInstance();
        return emailUtils.sendEmail(to, subject, body, from);
    }
}
