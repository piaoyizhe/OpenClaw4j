package com.openclaw.model.manager;

import com.openclaw.config.ConfigManager;
import com.openclaw.utils.DatabaseConnectionPool;
import com.openclaw.utils.OpenClawException;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * SQLite数据库管理器
 * 负责SQLite数据库的初始化、连接和管理，以及模糊搜索功能
 */
public class SQLiteManager {
    private static SQLiteManager instance;
    private ConfigManager configManager;
    private String dbPath;
    private DatabaseConnectionPool connectionPool;

    // 缓存机制（改进版）
    private LinkedHashMap<String, List<Map<String, Object>>> searchCache; // 使用LinkedHashMap实现LRU缓存
    private static final int CACHE_SIZE = 100; // 缓存大小

    /**
     * 获取单例实例
     */
    public static synchronized SQLiteManager getInstance() {
        if (instance == null) {
            instance = new SQLiteManager();
        }
        return instance;
    }

    /**
     * 构造方法
     */
    private SQLiteManager() {
        configManager = ConfigManager.getInstance();
        // 初始化LRU缓存
        searchCache = new LinkedHashMap<String, List<Map<String, Object>>>(CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, List<Map<String, Object>>> eldest) {
                return size() > CACHE_SIZE;
            }
        };
        initializeDatabase();
    }

    /**
     * 初始化数据库
     */
    private void initializeDatabase() {
        // 构建数据库路径
        String memoryDirectory = configManager.getMemoryDirectory();
        dbPath = memoryDirectory + File.separator + "openclaw.db";
        String dbUrl = "jdbc:sqlite:" + dbPath;

        try {
            // 初始化连接池
            connectionPool = new DatabaseConnectionPool(dbUrl, 5); // 最大5个连接
            connectionPool.initialize();

            // 创建数据表
            createTables();
        } catch (Exception e) {
            System.err.println("初始化SQLite数据库失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建数据表
     */
    private void createTables() {
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            Statement stmt = conn.createStatement();

            // 创建chunks表
            String createChunksTable = "" +
                    "CREATE TABLE IF NOT EXISTS chunks " +
                    "(" +
                    "    id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "    file_path TEXT NOT NULL, " +
                    "    start_line INTEGER, " +
                    "    end_line INTEGER, " +
                    "    content TEXT NOT NULL, " +
                    "    token_count INTEGER, " +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP " +
                    ")";
            stmt.execute(createChunksTable);

            // 创建metadata表
            String createMetadataTable = "" +
                    "CREATE TABLE IF NOT EXISTS metadata " +
                    "(" +
                    "    id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "    file_path TEXT NOT NULL UNIQUE, " +
                    "    file_size INTEGER, " +
                    "    last_modified TIMESTAMP, " +
                    "    md5_hash TEXT, " +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP " +
                    ")";
            stmt.execute(createMetadataTable);

            // 创建fts5索引表
            String createFtsTable = "" +
                    "CREATE VIRTUAL TABLE IF NOT EXISTS fts5_index USING fts5 " +
                    "(" +
                    "    content, " +
                    "    file_path, " +
                    "    tokenize='porter' " +
                    ")";
            stmt.execute(createFtsTable);

            // 创建索引
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_chunks_file_path ON chunks(file_path)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_chunks_created_at ON chunks(created_at)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_metadata_file_path ON metadata(file_path)");

            stmt.close();
            System.out.println("SQLite数据表创建成功");
        } catch (SQLException e) {
            System.err.println("创建SQLite数据表失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                connectionPool.returnConnection(conn);
            }
        }
    }

    /**
     * 获取数据库连接
     * @return 数据库连接
     */
    public Connection getConnection() {
        try {
            return connectionPool.getConnection();
        } catch (SQLException e) {
            System.err.println("获取数据库连接失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 归还数据库连接
     * @param connection 数据库连接
     */
    public void returnConnection(Connection connection) {
        connectionPool.returnConnection(connection);
    }

    /**
     * 关闭数据库连接
     */
    public void closeConnection() {
        connectionPool.close();
    }

    /**
     * 插入文本块
     * @param filePath 文件路径
     * @param startLine 起始行
     * @param endLine 结束行
     * @param content 内容
     * @param tokenCount 令牌数
     * @return 插入的ID
     */
    public int insertChunk(String filePath, int startLine, int endLine, String content, int tokenCount) {
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            String sql = "" +
                    "INSERT INTO chunks (file_path, start_line, end_line, content, token_count) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, filePath);
            pstmt.setInt(2, startLine);
            pstmt.setInt(3, endLine);
            pstmt.setString(4, content);
            pstmt.setInt(5, tokenCount);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    rs.close();
                    pstmt.close();

                    // 同时更新FTS5索引
                    updateFtsIndex(id, content, filePath);
                    
                    // 清除相关缓存
                    clearCache();
                    return id;
                }
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("插入文本块失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                connectionPool.returnConnection(conn);
            }
        }
        return -1;
    }

    /**
     * 批量插入文本块
     * @param chunks 文本块列表
     * @return 成功插入的数量
     */
    public int batchInsertChunks(List<Map<String, Object>> chunks) {
        if (chunks.isEmpty()) {
            return 0;
        }

        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement ftsPstmt = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 批量插入文本块
            String sql = "" +
                    "INSERT INTO chunks (file_path, start_line, end_line, content, token_count) " +
                    "VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);

            // 批量更新FTS5索引
            String ftsSql = "INSERT INTO fts5_index (content, file_path) VALUES (?, ?)";
            ftsPstmt = conn.prepareStatement(ftsSql);

            for (Map<String, Object> chunk : chunks) {
                // 插入文本块
                pstmt.setString(1, (String) chunk.get("file_path"));
                pstmt.setInt(2, (Integer) chunk.get("start_line"));
                pstmt.setInt(3, (Integer) chunk.get("end_line"));
                pstmt.setString(4, (String) chunk.get("content"));
                pstmt.setInt(5, (Integer) chunk.get("token_count"));
                pstmt.addBatch();

                // 插入FTS5索引
                ftsPstmt.setString(1, (String) chunk.get("content"));
                ftsPstmt.setString(2, (String) chunk.get("file_path"));
                ftsPstmt.addBatch();

                count++;

                // 每1000条执行一次批量操作
                if (count % 1000 == 0) {
                    pstmt.executeBatch();
                    ftsPstmt.executeBatch();
                    conn.commit();
                }
            }

            // 执行剩余的批量操作
            pstmt.executeBatch();
            ftsPstmt.executeBatch();
            conn.commit();

            // 清除缓存
            clearCache();

        } catch (SQLException e) {
            System.err.println("批量插入文本块失败: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            count = 0;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (ftsPstmt != null) ftsPstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return count;
    }

    /**
     * 清除缓存
     */
    private void clearCache() {
        if (searchCache.size() > CACHE_SIZE) {
            // 清除最早的缓存项
            Iterator<Map.Entry<String, List<Map<String, Object>>>> iterator = searchCache.entrySet().iterator();
            while (iterator.hasNext() && searchCache.size() > CACHE_SIZE / 2) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    /**
     * 从缓存获取搜索结果
     * @param keyword 关键词
     * @return 搜索结果
     */
    private List<Map<String, Object>> getFromCache(String keyword) {
        return searchCache.get(keyword);
    }

    /**
     * 将搜索结果存入缓存
     * @param keyword 关键词
     * @param results 搜索结果
     */
    private void putToCache(String keyword, List<Map<String, Object>> results) {
        searchCache.put(keyword, results);
        clearCache();
    }

    /**
     * 更新FTS5索引
     * @param chunkId 文本块ID
     * @param content 内容
     * @param filePath 文件路径
     */
    private void updateFtsIndex(int chunkId, String content, String filePath) {
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            String sql = "INSERT INTO fts5_index (content, file_path) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setString(2, filePath);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("更新FTS5索引失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                connectionPool.returnConnection(conn);
            }
        }
    }

    /**
     * 插入或更新文件元数据
     * @param filePath 文件路径
     * @param fileSize 文件大小
     * @param lastModified 最后修改时间
     * @param md5Hash MD5哈希值
     */
    public void upsertMetadata(String filePath, long fileSize, Timestamp lastModified, String md5Hash) {
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            String sql = "" +
                    "INSERT INTO metadata (file_path, file_size, last_modified, md5_hash) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT(file_path) DO UPDATE SET " +
                    "    file_size = excluded.file_size, " +
                    "    last_modified = excluded.last_modified, " +
                    "    md5_hash = excluded.md5_hash, " +
                    "    updated_at = CURRENT_TIMESTAMP";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, filePath);
            pstmt.setLong(2, fileSize);
            pstmt.setTimestamp(3, lastModified);
            pstmt.setString(4, md5Hash);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("插入或更新文件元数据失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                connectionPool.returnConnection(conn);
            }
        }
    }

    /**
     * 基于LIKE的基础模糊搜索
     * @param keyword 关键词
     * @param limit 限制结果数量
     * @return 搜索结果
     */
    public List<Map<String, Object>> basicFuzzySearch(String keyword, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            String sql = "" +
                    "SELECT c.id, c.file_path, c.start_line, c.end_line, c.content, c.token_count " +
                    "FROM chunks c " +
                    "WHERE c.content LIKE ? " +
                    "ORDER BY c.created_at DESC " +
                    "LIMIT ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setInt(2, limit);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", rs.getInt("id"));
                result.put("file_path", rs.getString("file_path"));
                result.put("start_line", rs.getInt("start_line"));
                result.put("end_line", rs.getInt("end_line"));
                result.put("content", rs.getString("content"));
                result.put("token_count", rs.getInt("token_count"));
                results.add(result);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("基础模糊搜索失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                connectionPool.returnConnection(conn);
            }
        }

        return results;
    }

    /**
     * 基于FTS5的高级模糊搜索
     * @param keyword 关键词
     * @param limit 限制结果数量
     * @return 搜索结果
     */
    public List<Map<String, Object>> advancedFuzzySearch(String keyword, int limit) {
        // 先从缓存获取
        List<Map<String, Object>> cachedResults = getFromCache(keyword);
        if (cachedResults != null) {
            // 如果缓存中有结果，返回前limit个
            return cachedResults.stream().limit(limit).collect(java.util.stream.Collectors.toList());
        }

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            // 使用参数化查询，避免SQL注入
            // 简化查询，不使用rank()函数，直接使用MATCH结果
            String sql = "" +
                    "SELECT c.id, c.file_path, c.start_line, c.end_line, c.content, c.token_count " +
                    "FROM chunks c, fts5_index " +
                    "WHERE c.file_path = fts5_index.file_path AND fts5_index.content MATCH ? " +
                    "LIMIT ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, keyword);
            pstmt.setInt(2, limit);

            // 执行查询
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", rs.getInt("id"));
                result.put("file_path", rs.getString("file_path"));
                result.put("start_line", rs.getInt("start_line"));
                result.put("end_line", rs.getInt("end_line"));
                result.put("content", rs.getString("content"));
                result.put("token_count", rs.getInt("token_count"));
                results.add(result);
            }
            rs.close();
            pstmt.close();

            // 将结果存入缓存
            putToCache(keyword, results);
        } catch (SQLException e) {
            System.err.println("高级模糊搜索失败: " + e.getMessage());
            e.printStackTrace();
            // 如果FTS5搜索失败，回退到基础搜索
            return basicFuzzySearch(keyword, limit);
        } finally {
            if (conn != null) {
                connectionPool.returnConnection(conn);
            }
        }

        return results;
    }

    /**
     * 混合搜索（结合基础搜索和高级搜索）
     * @param keyword 关键词
     * @param limit 限制结果数量
     * @return 搜索结果
     */
    public List<Map<String, Object>> hybridSearch(String keyword, int limit) {
        // 这里可以实现更复杂的混合搜索逻辑
        // 暂时返回高级搜索结果
        return advancedFuzzySearch(keyword, limit);
    }

    /**
     * 按时间范围搜索
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制结果数量
     * @return 搜索结果
     */
    public List<Map<String, Object>> searchByDateRange(Timestamp startDate, Timestamp endDate, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            String sql = "" +
                    "SELECT c.id, c.file_path, c.start_line, c.end_line, c.content, c.token_count " +
                    "FROM chunks c " +
                    "WHERE c.created_at BETWEEN ? AND ? " +
                    "ORDER BY c.created_at DESC " +
                    "LIMIT ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, startDate);
            pstmt.setTimestamp(2, endDate);
            pstmt.setInt(3, limit);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", rs.getInt("id"));
                result.put("file_path", rs.getString("file_path"));
                result.put("start_line", rs.getInt("start_line"));
                result.put("end_line", rs.getInt("end_line"));
                result.put("content", rs.getString("content"));
                result.put("token_count", rs.getInt("token_count"));
                results.add(result);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("按时间范围搜索失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                connectionPool.returnConnection(conn);
            }
        }

        return results;
    }

    /**
     * 清理过期数据
     * @param days 保留天数
     */
    public void cleanExpiredData(int days) {
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            String sql = "" +
                    "DELETE FROM chunks " +
                    "WHERE created_at < datetime('now', '-' || ? || ' days')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, days);
            int deleted = pstmt.executeUpdate();
            pstmt.close();

            // 清理对应的FTS5索引
            sql = "VACUUM fts5_index";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();

            System.out.println("清理过期数据完成，删除了 " + deleted + " 条记录");
        } catch (SQLException e) {
            System.err.println("清理过期数据失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                connectionPool.returnConnection(conn);
            }
        }
    }

    /**
     * 优化数据库
     */
    public void optimizeDatabase() {
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute("VACUUM");
            stmt.execute("ANALYZE");
            stmt.close();
            System.out.println("SQLite数据库优化完成");
        } catch (SQLException e) {
            System.err.println("优化数据库失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                connectionPool.returnConnection(conn);
            }
        }
    }

    /**
     * 获取数据库统计信息
     * @return 统计信息
     */
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            Statement stmt = conn.createStatement();

            // 获取chunks表统计
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM chunks");
            if (rs.next()) {
                stats.put("chunks_count", rs.getInt("count"));
            }
            rs.close();

            // 获取metadata表统计
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM metadata");
            if (rs.next()) {
                stats.put("metadata_count", rs.getInt("count"));
            }
            rs.close();

            stmt.close();
        } catch (SQLException e) {
            System.err.println("获取数据库统计信息失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                connectionPool.returnConnection(conn);
            }
        }

        return stats;
    }
}
