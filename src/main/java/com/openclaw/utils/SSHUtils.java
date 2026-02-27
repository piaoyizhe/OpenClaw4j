package com.openclaw.utils;

import com.jcraft.jsch.*;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class SSHUtils {
    private static SSHUtils instance;
    private Map<String, Session> sessions;
    private Map<String, Channel> channels;

    private SSHUtils() {
        sessions = new ConcurrentHashMap<>();
        channels = new ConcurrentHashMap<>();
    }

    public static SSHUtils getInstance() {
        if (instance == null) {
            synchronized (SSHUtils.class) {
                if (instance == null) {
                    instance = new SSHUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 连接到远程服务器
     * @param host 主机地址
     * @param port 端口（默认22）
     * @param username 用户名
     * @param password 密码
     * @return 连接结果
     */
    public String connect(String host, int port, String username, String password) {
        String sessionKey = username + "@" + host + ":" + port;

        try {
            if (sessions.containsKey(sessionKey)) {
                Session existingSession = sessions.get(sessionKey);
                if (existingSession.isConnected()) {
                    return "已连接到 " + sessionKey;
                }
            }

            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect(30000);
            sessions.put(sessionKey, session);

            return "成功连接到 " + host + ":" + port + "，用户: " + username;
        } catch (Exception e) {
            return "连接失败: " + e.getMessage();
        }
    }

    /**
     * 连接到远程服务器（使用密钥）
     * @param host 主机地址
     * @param port 端口
     * @param username 用户名
     * @param privateKey 私钥
     * @param passphrase 密钥密码（可选）
     * @return 连接结果
     */
    public String connectWithKey(String host, int port, String username, String privateKey, String passphrase) {
        String sessionKey = username + "@" + host + ":" + port;

        try {
            if (sessions.containsKey(sessionKey)) {
                Session existingSession = sessions.get(sessionKey);
                if (existingSession.isConnected()) {
                    return "已连接到 " + sessionKey;
                }
            }

            JSch jsch = new JSch();
            if (passphrase != null && !passphrase.isEmpty()) {
                jsch.addIdentity("temp", privateKey.getBytes(), null, passphrase.getBytes());
            } else {
                jsch.addIdentity("temp", privateKey.getBytes(), null, null);
            }

            Session session = jsch.getSession(username, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);
            sessions.put(sessionKey, session);

            return "成功连接到 " + host + ":" + port + "，用户: " + username;
        } catch (Exception e) {
            return "连接失败: " + e.getMessage();
        }
    }

    /**
     * 在已连接的会话上执行命令
     * @param host 主机地址
     * @param username 用户名
     * @param command 要执行的命令
     * @return 命令执行结果
     */
    public String execute(String host, String username, String command) {
        return execute(host, 22, username, command);
    }

    /**
     * 在已连接的会话上执行命令
     * @param host 主机地址
     * @param port 端口
     * @param username 用户名
     * @param command 要执行的命令
     * @return 命令执行结果
     */
    public String execute(String host, int port, String username, String command) {
        String sessionKey = username + "@" + host + ":" + port;
        Session session = sessions.get(sessionKey);

        if (session == null || !session.isConnected()) {
            return "错误: 未连接到 " + sessionKey + "，请先使用 ssh_connect 连接";
        }

        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            java.io.InputStream in = channel.getInputStream();
            channel.connect(30000);

            StringBuilder output = new StringBuilder();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    break;
                }
                Thread.sleep(100);
            }

            channel.disconnect();
            return output.toString();
        } catch (Exception e) {
            return "执行命令失败: " + e.getMessage();
        }
    }

    /**
     * 断开连接
     * @param host 主机地址
     * @param username 用户名
     * @return 断开结果
     */
    public String disconnect(String host, String username) {
        return disconnect(host, 22, username);
    }

    /**
     * 断开连接
     * @param host 主机地址
     * @param port 端口
     * @param username 用户名
     * @return 断开结果
     */
    public String disconnect(String host, int port, String username) {
        String sessionKey = username + "@" + host + ":" + port;
        Session session = sessions.get(sessionKey);

        if (session == null) {
            return "未找到连接: " + sessionKey;
        }

        if (session.isConnected()) {
            session.disconnect();
        }
        sessions.remove(sessionKey);

        return "已断开连接: " + sessionKey;
    }

    /**
     * 断开所有连接
     */
    public void disconnectAll() {
        for (Session session : sessions.values()) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
        sessions.clear();
        channels.clear();
    }

    /**
     * 检查是否已连接
     * @param host 主机地址
     * @param username 用户名
     * @return 是否已连接
     */
    public boolean isConnected(String host, String username) {
        return isConnected(host, 22, username);
    }

    /**
     * 检查是否已连接
     * @param host 主机地址
     * @param port 端口
     * @param username 用户名
     * @return 是否已连接
     */
    public boolean isConnected(String host, int port, String username) {
        String sessionKey = username + "@" + host + ":" + port;
        Session session = sessions.get(sessionKey);
        return session != null && session.isConnected();
    }

    /**
     * 获取所有活动连接
     * @return 活动连接列表
     */
    public String listConnections() {
        if (sessions.isEmpty()) {
            return "没有活动的SSH连接";
        }

        StringBuilder sb = new StringBuilder("活动的SSH连接:\n");
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            String key = entry.getKey();
            Session session = entry.getValue();
            sb.append("- ").append(key);
            sb.append(session.isConnected() ? " [已连接]" : " [已断开]");
            sb.append("\n");
        }
        return sb.toString();
    }
}
