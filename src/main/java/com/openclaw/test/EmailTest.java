package com.openclaw.test;

import com.openclaw.utils.EmailUtils;

/**
 * 邮件发送测试类
 */
public class EmailTest {
    public static void main(String[] args) {
        try {
            // 创建EmailUtils实例
            EmailUtils emailUtils = EmailUtils.getInstance();
            
            // 发送测试邮件
            String to = "591543227@qq.com";
            String subject = "测试邮件";
            String body = "这是一封测试邮件，用于测试邮件发送功能是否正常工作。";
            
            System.out.println("开始发送邮件...");
            String result = emailUtils.sendEmail(to, subject, body);
            System.out.println("发送结果: " + result);
        } catch (Exception e) {
            System.err.println("发送邮件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}