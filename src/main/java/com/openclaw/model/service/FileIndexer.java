package com.openclaw.model.service;

import com.openclaw.config.ConfigManager;
import com.openclaw.model.manager.SQLiteManager;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * 文件索引器
 * 负责文件的索引和更新，确保文件内容能够被正确索引到SQLite数据库中
 */
public class FileIndexer {
    private static FileIndexer instance;
    private ConfigManager configManager;
    private SQLiteManager sqliteManager;
    private String memoryDirectory;
    private ExecutorService executorService;
    private WatchService watchService;
    private boolean running;

    // 分块参数
    private static final int DEFAULT_CHUNK_SIZE = 1600; // 默认分块大小（字符）
    private static final int DEFAULT_OVERLAP_SIZE = 80; // 默认重叠大小（字符）

    /**
     * 私有构造方法
     */
    private FileIndexer() {
        configManager = ConfigManager.getInstance();
        sqliteManager = SQLiteManager.getInstance();
        memoryDirectory = configManager.getMemoryDirectory();
        executorService = Executors.newFixedThreadPool(4);
        running = false;
        initializeWatchService();
    }

    /**
     * 获取单例实例
     * @return FileIndexer实例
     */
    public static FileIndexer getInstance() {
        if (instance == null) {
            synchronized (FileIndexer.class) {
                if (instance == null) {
                    instance = new FileIndexer();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化文件监控服务
     */
    private void initializeWatchService() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(memoryDirectory);
            path.register(watchService, 
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            System.out.println("文件监控服务初始化成功: " + memoryDirectory);
        } catch (IOException e) {
            System.err.println("初始化文件监控服务失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 开始监控文件变化
     */
    public void startWatching() {
        running = true;
        executorService.submit(this::watchFiles);
        System.out.println("文件监控已启动");
    }

    /**
     * 停止监控文件变化
     */
    public void stopWatching() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("文件监控已停止");
    }

    /**
     * 监控文件变化
     */
    private void watchFiles() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileName = ev.context();
                Path fullPath = Paths.get(memoryDirectory).resolve(fileName);

                if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    if (fullPath.toString().endsWith(".md")) {
                        executorService.submit(() -> indexFile(fullPath.toString()));
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    // 处理文件删除
                    executorService.submit(() -> handleFileDeletion(fullPath.toString()));
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    /**
     * 索引单个文件
     * @param filePath 文件路径
     */
    public void indexFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.err.println("文件不存在或不是普通文件: " + filePath);
            return;
        }

        try {
            // 读取文件内容
            String content = new String(Files.readAllBytes(file.toPath()));

            // 检查内容是否为null或空
            if (content == null || content.isEmpty()) {
                System.err.println("文件内容为空: " + filePath);
                return;
            }

            // 计算文件哈希值
            String md5Hash = calculateMD5(file);

            // 更新文件元数据
            sqliteManager.upsertMetadata(
                    filePath,
                    file.length(),
                    new Timestamp(file.lastModified()),
                    md5Hash
            );

            // 分块处理
            List<TextChunk> chunks = splitIntoChunks(content);

            // 索引每个块
            for (int i = 0; i < chunks.size(); i++) {
                TextChunk chunk = chunks.get(i);
                sqliteManager.insertChunk(
                        filePath,
                        chunk.getStartLine(),
                        chunk.getEndLine(),
                        chunk.getContent(),
                        chunk.getTokenCount()
                );
            }
//            System.out.println("文件索引完成: " + filePath + "，生成了 " + chunks.size() + " 个块");
        } catch (Exception e) {
            System.err.println("索引文件失败: " + filePath + "，错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理文件删除
     * @param filePath 文件路径
     */
    private void handleFileDeletion(String filePath) {
        // 这里可以实现从数据库中删除对应索引的逻辑
        System.out.println("文件已删除: " + filePath);
    }

    /**
     * 将文本分割成块
     * @param content 文本内容
     * @return 文本块列表
     */
    private List<TextChunk> splitIntoChunks(String content) {
        List<TextChunk> chunks = new ArrayList<>();
        String[] lines = content.split("\\n");

        int currentLine = 1;
        StringBuilder currentChunk = new StringBuilder();
        int chunkSize = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineSize = line.length();

            if (chunkSize + lineSize > DEFAULT_CHUNK_SIZE) {
                // 完成当前块
                TextChunk chunk = new TextChunk();
                chunk.setContent(currentChunk.toString());
                chunk.setStartLine(currentLine);
                chunk.setEndLine(i);
                chunk.setTokenCount(estimateTokenCount(currentChunk.toString()));
                chunks.add(chunk);

                // 开始新块，保留重叠部分
                currentChunk = new StringBuilder();
                // 计算重叠行数
                int overlapLines = Math.min(3, chunks.size());
                for (int j = Math.max(0, i - overlapLines); j < i; j++) {
                    currentChunk.append(lines[j]).append("\n");
                }
                currentLine = i - overlapLines + 1;
                if (currentLine < 1) currentLine = 1;
                chunkSize = currentChunk.length();
            }

            currentChunk.append(line).append("\n");
            chunkSize += lineSize + 1; // +1 for newline
        }

        // 处理最后一个块
        if (currentChunk.length() > 0) {
            TextChunk chunk = new TextChunk();
            chunk.setContent(currentChunk.toString());
            chunk.setStartLine(currentLine);
            chunk.setEndLine(lines.length);
            chunk.setTokenCount(estimateTokenCount(currentChunk.toString()));
            chunks.add(chunk);
        }

        return chunks;
    }

    /**
     * 估算令牌数
     * @param text 文本内容
     * @return 估算的令牌数
     */
    private int estimateTokenCount(String text) {
        // 简单估算：假设平均每个令牌由4个字符组成
        return text.length() / 4;
    }

    /**
     * 计算文件的MD5哈希值
     * @param file 文件
     * @return MD5哈希值
     */
    private String calculateMD5(File file) {
        try (InputStream is = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                md.update(buffer, 0, read);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("计算文件MD5哈希值失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 批量索引目录中的所有文件
     * @param directoryPath 目录路径
     */
    public void indexDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("目录不存在或不是目录: " + directoryPath);
            return;
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".md"));
        if (files == null || files.length == 0) {
            System.out.println("目录中没有找到Markdown文件: " + directoryPath);
            return;
        }

        System.out.println("开始批量索引目录: " + directoryPath + "，共 " + files.length + " 个文件");

        for (File file : files) {
            indexFile(file.getAbsolutePath());
        }

        System.out.println("目录索引完成: " + directoryPath);
    }

    /**
     * 索引所有记忆文件
     */
    public void indexAllMemoryFiles() {
        // 索引主记忆目录
        indexDirectory(memoryDirectory);

        // 索引归档目录
        File archiveDir = new File(memoryDirectory, "archive");
        if (archiveDir.exists() && archiveDir.isDirectory()) {
            File[] monthDirs = archiveDir.listFiles(File::isDirectory);
            if (monthDirs != null) {
                for (File monthDir : monthDirs) {
                    indexDirectory(monthDir.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 文本块类
     */
    private static class TextChunk {
        private String content;
        private int startLine;
        private int endLine;
        private int tokenCount;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getStartLine() {
            return startLine;
        }

        public void setStartLine(int startLine) {
            this.startLine = startLine;
        }

        public int getEndLine() {
            return endLine;
        }

        public void setEndLine(int endLine) {
            this.endLine = endLine;
        }

        public int getTokenCount() {
            return tokenCount;
        }

        public void setTokenCount(int tokenCount) {
            this.tokenCount = tokenCount;
        }
    }

    /**
     * 关闭资源
     */
    public void close() {
        stopWatching();
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException e) {
            System.err.println("关闭watchService失败: " + e.getMessage());
        }
    }
}
