package com.openclaw.utils;

import com.openclaw.config.ConfigManager;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 邮件发送工具类
 */
public class EmailUtils {
    private static EmailUtils instance;
    private ConfigManager configManager;

    /**
     * 私有构造方法
     */
    private EmailUtils() {
        configManager = ConfigManager.getInstance();
    }

    /**
     * 获取单例实例
     * @return EmailUtils实例
     */
    public static EmailUtils getInstance() {
        if (instance == null) {
            synchronized (EmailUtils.class) {
                if (instance == null) {
                    instance = new EmailUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 发送邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param body 邮件正文
     * @param from 发件人邮箱（用于显示发送人）
     * @return 发送结果
     * @throws Exception 发送异常
     */
    public String sendEmail(String to, String subject, String body, String from) throws Exception {
        // 检查邮件配置是否启用
        if (!configManager.isMailEnabled()) {
            return "邮件发送功能未启用";
        }

        // 获取邮件配置
        String host = configManager.getMailHost();
        int port = configManager.getMailPort();
        String username = configManager.getMailUsername();
        String password = configManager.getMailPassword();
        String senderName = configManager.getMailSenderName();
        boolean ssl = configManager.isMailSsl();
        int timeout = configManager.getMailTimeout();


        // 验证配置
        if (username.isEmpty() || password.isEmpty() || from.isEmpty()) {
            return "邮件配置不完整";
        }

        // 验证收件人
        if (to == null || to.isEmpty()) {
            return "收件人邮箱不能为空";
        }

        // 设置邮件属性
        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.port", String.valueOf(port));
        props.setProperty("mail.smtp.timeout", String.valueOf(timeout));

        // 启用SSL
        if (ssl) {
            props.setProperty("mail.smtp.ssl.enable", "true");
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
        }

        // 创建会话
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebug(false); // 设置为true可以查看调试信息

        // 创建邮件消息
        MimeMessage message = new MimeMessage(session);
        // 如果有发送人名，使用"姓名 <邮箱>"格式
        if (senderName != null && !senderName.isEmpty()) {
            message.setFrom(new InternetAddress(from, senderName, "UTF-8"));
        } else {
            message.setFrom(new InternetAddress(from));
        }
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, "UTF-8");
        message.setText(body, "UTF-8");
        message.setReplyTo(new Address[]{new InternetAddress(from)});

        // 发送邮件
        Transport.send(message);

        return "邮件发送成功\n收件人: " + to + "\n主题: " + subject;
    }

    /**
     * 发送HTML格式邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param htmlBody HTML正文
     * @return 发送结果
     * @throws Exception 发送异常
     */
    public String sendHtmlEmail(String to, String subject, String htmlBody) throws Exception {
        // 检查邮件配置是否启用
        if (!configManager.isMailEnabled()) {
            return "邮件发送功能未启用";
        }

        // 获取邮件配置
        String host = configManager.getMailHost();
        int port = configManager.getMailPort();
        String username = configManager.getMailUsername();
        String password = configManager.getMailPassword();
        String from = configManager.getMailFrom();
        String senderName = configManager.getMailSenderName();
        boolean ssl = configManager.isMailSsl();
        int timeout = configManager.getMailTimeout();

        // 验证配置
        if (username.isEmpty() || password.isEmpty() || from.isEmpty()) {
            return "邮件配置不完整";
        }

        // 验证收件人
        if (to == null || to.isEmpty()) {
            return "收件人邮箱不能为空";
        }

        // 设置邮件属性
        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.port", String.valueOf(port));
        props.setProperty("mail.smtp.timeout", String.valueOf(timeout));

        // 启用SSL
        if (ssl) {
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
        }

        // 创建会话
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebug(false);

        // 创建邮件消息
        MimeMessage message = new MimeMessage(session);
        // 如果有发送人名，使用"姓名 <邮箱>"格式
        if (senderName != null && !senderName.isEmpty()) {
            message.setFrom(new InternetAddress(from, senderName, "UTF-8"));
        } else {
            message.setFrom(new InternetAddress(from));
        }
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, "UTF-8");
        message.setContent(htmlBody, "text/html;charset=UTF-8");
        message.setReplyTo(new Address[]{new InternetAddress(from)});

        // 发送邮件
        Transport.send(message);

        return "HTML邮件发送成功\n收件人: " + to + "\n主题: " + subject;
    }
}
