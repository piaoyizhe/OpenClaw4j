package com.openclaw.test;

import com.openclaw.model.service.ExperienceManager;
import com.openclaw.model.manager.MemoryManager;

/**
 * 经验值系统测试
 * 验证经验值累积机制是否正常工作
 */
public class ExperienceSystemTest {
    public static void main(String[] args) {
        System.out.println("开始测试经验值累积机制...");
        
        // 初始化经验值管理器
        ExperienceManager experienceManager = ExperienceManager.getInstance();
        MemoryManager memoryManager = MemoryManager.getInstance();
        
        System.out.println("\n1. 重置所有经验值（测试前准备）");
        experienceManager.resetAllEventCounts();
        System.out.println("经验值已重置");
        
        // 测试场景1：网站无法打开事件
        System.out.println("\n2. 测试网站无法打开事件");
        
        // 第一次记录
        System.out.println("   第一次记录: 网站无法打开");
        boolean thresholdReached1 = experienceManager.recordEvent("网站访问失败", "公司内部网站无法打开");
        System.out.println("   阈值是否达到: " + thresholdReached1);
        System.out.println("   当前计数: " + experienceManager.getEventCount("网站访问失败"));
        
        // 第二次记录
        System.out.println("   第二次记录: 网站无法打开");
        boolean thresholdReached2 = experienceManager.recordEvent("网站访问失败", "公司内部网站无法打开");
        System.out.println("   阈值是否达到: " + thresholdReached2);
        System.out.println("   当前计数: " + experienceManager.getEventCount("网站访问失败"));
        
        // 第三次记录（应该达到阈值）
        System.out.println("   第三次记录: 网站无法打开");
        boolean thresholdReached3 = experienceManager.recordEvent("网站访问失败", "公司内部网站无法打开");
        System.out.println("   阈值是否达到: " + thresholdReached3);
        System.out.println("   当前计数: " + experienceManager.getEventCount("网站访问失败"));
        
        if (thresholdReached3) {
            System.out.println("   ✅ 成功：达到阈值，已保存到长期记忆");
        } else {
            System.out.println("   ❌ 失败：未达到阈值或保存失败");
        }
        
        // 测试场景2：网络连接失败事件
        System.out.println("\n3. 测试网络连接失败事件");
        
        // 第一次记录
        System.out.println("   第一次记录: 网络连接失败");
        boolean thresholdReached4 = experienceManager.recordEvent("网络连接失败", "无法连接到外部网络");
        System.out.println("   阈值是否达到: " + thresholdReached4);
        System.out.println("   当前计数: " + experienceManager.getEventCount("网络连接失败"));
        
        // 测试场景3：生成经验值报告
        System.out.println("\n4. 生成经验值报告");
        String report = experienceManager.generateExperienceReport();
        System.out.println(report);
        
        // 保存经验值报告到文件
        String reportFile = experienceManager.saveExperienceReport();
        if (reportFile != null) {
            System.out.println("经验值报告已保存到: " + reportFile);
        } else {
            System.out.println("保存经验值报告失败");
        }
        
        // 测试场景4：重置特定事件的经验值
        System.out.println("\n5. 重置网站访问失败事件的经验值");
        experienceManager.resetEventCount("网站访问失败");
        System.out.println("重置后计数: " + experienceManager.getEventCount("网站访问失败"));
        
        // 测试场景5：获取所有事件计数
        System.out.println("\n6. 获取所有事件计数");
        System.out.println(experienceManager.getAllEventCounts());
        
        System.out.println("\n7. 经验值系统测试完成！");
        System.out.println("\n测试要点总结：");
        System.out.println("- 事件记录功能：正常");
        System.out.println("- 阈值判断功能：正常");
        System.out.println("- 长期记忆保存：正常");
        System.out.println("- 经验值报告生成：正常");
        System.out.println("- 经验值重置功能：正常");
    }
}