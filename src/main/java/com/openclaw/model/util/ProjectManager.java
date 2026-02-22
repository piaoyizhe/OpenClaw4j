package com.openclaw.model.util;

import com.openclaw.config.ConfigManager;
import com.openclaw.utils.LLMClient;
import com.openclaw.model.entity.Conversation;
import com.openclaw.model.entity.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 项目文件夹管理器
 * 负责管理项目相关内容，包括项目介绍、会议记录、项目文件等
 */
public class ProjectManager {
    private static ProjectManager instance;
    private ConfigManager configManager;
    private LLMClient llmClient;
    private String memoryDirectory;
    private String projectsDirectory;
    
    /**
     * 私有构造方法
     */
    private ProjectManager() {
        configManager = ConfigManager.getInstance();
        llmClient = LLMClient.getInstance();
        memoryDirectory = configManager.getMemoryDirectory();
        projectsDirectory = memoryDirectory + File.separator + "projects";
        
        initializeDirectories();
    }
    
    /**
     * 获取单例实例
     * @return ProjectManager实例
     */
    public static ProjectManager getInstance() {
        if (instance == null) {
            synchronized (ProjectManager.class) {
                if (instance == null) {
                    instance = new ProjectManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化目录
     */
    private void initializeDirectories() {
        File projectsDir = new File(projectsDirectory);
        if (!projectsDir.exists()) {
            if (!projectsDir.mkdirs()) {
                System.err.println("创建项目目录失败: " + projectsDirectory);
            }
        }
    }
    
    /**
     * 创建项目
     * @param projectName 项目名称
     * @return 项目目录路径
     */
    public String createProject(String projectName) {
        try {
            String projectDirName = sanitizeProjectName(projectName);
            Path projectPath = Paths.get(projectsDirectory, projectDirName);
            
            if (Files.exists(projectPath)) {
                System.out.println("项目已存在: " + projectName);
                return projectPath.toString();
            }
            
            // 创建项目目录结构
            Files.createDirectories(projectPath);
            Files.createDirectories(projectPath.resolve("meetings"));
            Files.createDirectories(projectPath.resolve("files"));
            
            // 创建项目介绍文件
            Path infoPath = projectPath.resolve("project_info.md");
            String infoContent = "# " + projectName + "\n\n" +
                               "## 项目介绍\n" +
                               "项目创建时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n\n" +
                               "## 项目状态\n" +
                               "- 进行中\n\n";
            Files.write(infoPath, infoContent.getBytes());
            
            // 创建项目记忆文件
            Path memoryPath = projectPath.resolve("project_memory.md");
            String memoryContent = "# " + projectName + " 项目记忆\n\n" +
                                 "## 关键信息\n\n" +
                                 "## 会议纪要索引\n\n" +
                                 "## 项目文件索引\n\n";
            Files.write(memoryPath, memoryContent.getBytes());
            
            System.out.println("项目创建成功: " + projectName);
            return projectPath.toString();
            
        } catch (IOException e) {
            System.err.println("创建项目失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 清理项目名称
     * @param projectName 原始项目名称
     * @return 清理后的项目名称
     */
    private String sanitizeProjectName(String projectName) {
        return projectName.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5_-]", "_");
    }
    
    /**
     * 从对话中提取项目内容
     * @param conversation 对话对象
     * @return 提取的项目信息
     */
    public String extractProjectContent(Conversation conversation) {
        try {
            StringBuilder content = new StringBuilder();
            for (Message message : conversation.getMessages()) {
                content.append(message.getRole()).append(": ").append(message.getContent()).append("\n\n");
            }
            
            String prompt = "请分析以下对话内容，判断是否涉及项目相关内容。如果涉及，请提取：\n" +
                          "1. 项目名称\n" +
                          "2. 项目介绍\n" +
                          "3. 关键信息\n" +
                          "4. 是否是会议内容\n\n" +
                          "对话内容:\n" + content.toString();
            
            return llmClient.generateResponse(prompt);
            
        } catch (Exception e) {
            System.err.println("提取项目内容失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 添加项目文件
     * @param projectName 项目名称
     * @param sourceFilePath 源文件路径
     * @return 是否成功
     */
    public boolean addProjectFile(String projectName, String sourceFilePath) {
        try {
            String projectDirName = sanitizeProjectName(projectName);
            Path projectPath = Paths.get(projectsDirectory, projectDirName);
            
            if (!Files.exists(projectPath)) {
                System.err.println("项目不存在: " + projectName);
                return false;
            }
            
            Path sourcePath = Paths.get(sourceFilePath);
            if (!Files.exists(sourcePath)) {
                System.err.println("源文件不存在: " + sourceFilePath);
                return false;
            }
            
            // 复制文件到项目files目录
            Path targetPath = projectPath.resolve("files").resolve(sourcePath.getFileName());
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 更新项目记忆，添加文件索引
            updateProjectMemoryWithFile(projectName, sourcePath.getFileName().toString(), targetPath.toString());
            
            System.out.println("文件添加成功: " + sourcePath.getFileName());
            return true;
            
        } catch (IOException e) {
            System.err.println("添加项目文件失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新项目记忆，添加文件索引
     */
    private void updateProjectMemoryWithFile(String projectName, String fileName, String filePath) {
        try {
            String projectDirName = sanitizeProjectName(projectName);
            Path memoryPath = Paths.get(projectsDirectory, projectDirName, "project_memory.md");
            
            String currentContent = new String(Files.readAllBytes(memoryPath));
            String fileEntry = "- [" + fileName + "](" + filePath + ")\n";
            
            if (!currentContent.contains("## 项目文件索引")) {
                currentContent += "\n## 项目文件索引\n";
            }
            
            if (!currentContent.contains(fileName)) {
                currentContent = currentContent.replace("## 项目文件索引", "## 项目文件索引\n" + fileEntry);
                Files.write(memoryPath, currentContent.getBytes());
            }
            
        } catch (IOException e) {
            System.err.println("更新项目记忆失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加会议记录
     * @param projectName 项目名称
     * @param content 会议内容
     * @return 会议文件路径
     */
    public String addMeetingNote(String projectName, String content) {
        try {
            String projectDirName = sanitizeProjectName(projectName);
            Path projectPath = Paths.get(projectsDirectory, projectDirName);
            
            if (!Files.exists(projectPath)) {
                System.err.println("项目不存在: " + projectName);
                return null;
            }
            
            // 创建会议文件
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
            String meetingFileName = timestamp + "_meeting.md";
            Path meetingPath = projectPath.resolve("meetings").resolve(meetingFileName);
            
            String meetingContent = "# 会议记录 - " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "\n\n" +
                                  "## 会议内容\n" + content + "\n\n";
            Files.write(meetingPath, meetingContent.getBytes());
            
            // 更新项目记忆，添加会议索引
            updateProjectMemoryWithMeeting(projectName, meetingFileName, meetingPath.toString());
            
            System.out.println("会议记录添加成功: " + meetingFileName);
            return meetingPath.toString();
            
        } catch (IOException e) {
            System.err.println("添加会议记录失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 更新项目记忆，添加会议索引
     */
    private void updateProjectMemoryWithMeeting(String projectName, String meetingName, String meetingPath) {
        try {
            String projectDirName = sanitizeProjectName(projectName);
            Path memoryPath = Paths.get(projectsDirectory, projectDirName, "project_memory.md");
            
            String currentContent = new String(Files.readAllBytes(memoryPath));
            String meetingEntry = "- [" + meetingName + "](" + meetingPath + ")\n";
            
            if (!currentContent.contains("## 会议纪要索引")) {
                currentContent += "\n## 会议纪要索引\n";
            }
            
            if (!currentContent.contains(meetingName)) {
                currentContent = currentContent.replace("## 会议纪要索引", "## 会议纪要索引\n" + meetingEntry);
                Files.write(memoryPath, currentContent.getBytes());
            }
            
        } catch (IOException e) {
            System.err.println("更新项目记忆失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索项目记忆
     * @param projectName 项目名称
     * @param keyword 关键词
     * @return 搜索结果
     */
    public List<String> searchProjectMemory(String projectName, String keyword) {
        List<String> results = new ArrayList<>();
        try {
            String projectDirName = sanitizeProjectName(projectName);
            Path projectPath = Paths.get(projectsDirectory, projectDirName);
            
            if (!Files.exists(projectPath)) {
                System.err.println("项目不存在: " + projectName);
                return results;
            }
            
            // 搜索项目记忆文件
            Path memoryPath = projectPath.resolve("project_memory.md");
            if (Files.exists(memoryPath)) {
                String content = new String(Files.readAllBytes(memoryPath));
                if (content.contains(keyword)) {
                    results.add("项目记忆: 找到匹配内容");
                }
            }
            
            // 搜索会议记录
            Path meetingsPath = projectPath.resolve("meetings");
            File meetingsDir = meetingsPath.toFile();
            File[] meetingFiles = meetingsDir.listFiles((dir, name) -> name.endsWith(".md"));
            if (meetingFiles != null) {
                for (File file : meetingFiles) {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    if (content.contains(keyword)) {
                        results.add("会议记录 " + file.getName() + ": 找到匹配内容");
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("搜索项目记忆失败: " + e.getMessage());
        }
        return results;
    }
    
    /**
     * 读取项目文件
     * @param projectName 项目名称
     * @param fileName 文件名
     * @return 文件内容
     */
    public String readProjectFile(String projectName, String fileName) {
        try {
            String projectDirName = sanitizeProjectName(projectName);
            Path projectPath = Paths.get(projectsDirectory, projectDirName);
            
            if (!Files.exists(projectPath)) {
                System.err.println("项目不存在: " + projectName);
                return null;
            }
            
            // 先在files目录找
            Path filePath = projectPath.resolve("files").resolve(fileName);
            if (!Files.exists(filePath)) {
                // 再在meetings目录找
                filePath = projectPath.resolve("meetings").resolve(fileName);
                if (!Files.exists(filePath)) {
                    // 最后在项目根目录找
                    filePath = projectPath.resolve(fileName);
                    if (!Files.exists(filePath)) {
                        System.err.println("文件不存在: " + fileName);
                        return null;
                    }
                }
            }
            
            return new String(Files.readAllBytes(filePath));
            
        } catch (IOException e) {
            System.err.println("读取项目文件失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取所有项目列表
     * @return 项目名称列表
     */
    public List<String> getAllProjects() {
        List<String> projects = new ArrayList<>();
        File projectsDir = new File(projectsDirectory);
        File[] projectDirs = projectsDir.listFiles(File::isDirectory);
        
        if (projectDirs != null) {
            for (File dir : projectDirs) {
                projects.add(dir.getName());
            }
        }
        return projects;
    }
    
    /**
     * 更新项目介绍
     * @param projectName 项目名称
     * @param newInfo 新的项目介绍
     * @return 是否成功
     */
    public boolean updateProjectInfo(String projectName, String newInfo) {
        try {
            String projectDirName = sanitizeProjectName(projectName);
            Path infoPath = Paths.get(projectsDirectory, projectDirName, "project_info.md");
            
            if (!Files.exists(infoPath)) {
                System.err.println("项目信息文件不存在: " + projectName);
                return false;
            }
            
            String currentContent = new String(Files.readAllBytes(infoPath));
            String updatedContent = currentContent + "\n## 更新记录\n" + 
                                   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n" +
                                   newInfo + "\n";
            Files.write(infoPath, updatedContent.getBytes());
            
            System.out.println("项目介绍更新成功");
            return true;
            
        } catch (IOException e) {
            System.err.println("更新项目介绍失败: " + e.getMessage());
            return false;
        }
    }
}
