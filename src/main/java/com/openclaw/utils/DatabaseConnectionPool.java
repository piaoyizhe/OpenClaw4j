package com.openclaw.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 数据库连接池
 * 用于管理SQLite数据库连接
 */
public class DatabaseConnectionPool {
    private final String url;
    private final int maxPoolSize;
    private final BlockingQueue<Connection> connectionPool;
    private boolean initialized;

    /**
     * 构造方法
     * @param url 数据库连接URL
     * @param maxPoolSize 最大连接数
     */
    public DatabaseConnectionPool(String url, int maxPoolSize) {
        this.url = url;
        this.maxPoolSize = maxPoolSize;
        this.connectionPool = new LinkedBlockingQueue<>(maxPoolSize);
        this.initialized = false;
    }

    /**
     * 初始化连接池
     * @throws SQLException SQL异常
     */
    public synchronized void initialize() throws SQLException {
        if (initialized) {
            return;
        }

        // 加载SQLite驱动
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("无法加载SQLite驱动", e);
        }

        // 初始化连接池
        for (int i = 0; i < maxPoolSize; i++) {
            try {
                Connection connection = DriverManager.getConnection(url);
                connection.setAutoCommit(true);
                connectionPool.offer(connection);
            } catch (SQLException e) {
                System.err.println("初始化数据库连接失败: " + e.getMessage());
                // 继续尝试创建其他连接
            }
        }

        if (connectionPool.isEmpty()) {
            throw new SQLException("无法创建任何数据库连接");
        }

        initialized = true;
        System.out.println("数据库连接池初始化完成，可用连接数: " + connectionPool.size());
    }

    /**
     * 获取数据库连接
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    public Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }

        try {
            Connection connection = connectionPool.poll(30, TimeUnit.SECONDS);
            if (connection == null) {
                throw new SQLException("获取数据库连接超时");
            }

            // 检查连接是否有效
            if (connection.isClosed() || !connection.isValid(5)) {
                // 连接无效，创建新连接
                connection = DriverManager.getConnection(url);
                connection.setAutoCommit(true);
            }

            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取数据库连接被中断", e);
        }
    }

    /**
     * 归还数据库连接
     * @param connection 数据库连接
     */
    public void returnConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.isClosed()) {
                connection.setAutoCommit(true);
                connectionPool.offer(connection);
            }
        } catch (SQLException e) {
            System.err.println("归还数据库连接失败: " + e.getMessage());
        }
    }

    /**
     * 关闭连接池
     */
    public synchronized void close() {
        while (!connectionPool.isEmpty()) {
            Connection connection = connectionPool.poll();
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("关闭数据库连接失败: " + e.getMessage());
                }
            }
        }
        initialized = false;
        System.out.println("数据库连接池已关闭");
    }

    /**
     * 获取连接池大小
     * @return 连接池大小
     */
    public int getPoolSize() {
        return connectionPool.size();
    }

    /**
     * 获取最大连接数
     * @return 最大连接数
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
}
