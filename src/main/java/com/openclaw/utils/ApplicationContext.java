package com.openclaw.utils;

import com.openclaw.config.ConfigManager;
import com.openclaw.brain.Hippocampus;
import com.openclaw.brain.Prefrontal;
import com.openclaw.model.manager.*;
import com.openclaw.model.service.*;
import com.openclaw.tools.ToolManagerRegistry;

/**
 * 应用程序上下文
 * 用于管理所有组件的生命周期和依赖关系
 */
public class ApplicationContext {
    private static ApplicationContext instance;
    private final DependencyInjector injector;

    /**
     * 私有构造方法
     */
    private ApplicationContext() {
        injector = DependencyInjector.getInstance();
        initializeComponents();
    }

    /**
     * 获取单例实例
     */
    public static synchronized ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    /**
     * 初始化所有组件
     */
    private void initializeComponents() {
        // 注册配置管理器
        ConfigManager configManager = ConfigManager.getInstance();
        injector.registerSingleton(ConfigManager.class, configManager);

        // 注册工具类
        LLMClient llmClient = LLMClient.getInstance();
        injector.registerSingleton(LLMClient.class, llmClient);

        // 注册管理器
        injector.registerFactory(DailyLogManager.class, injector -> DailyLogManager.getInstance());
        injector.registerFactory(SQLiteManager.class, injector -> SQLiteManager.getInstance());
        injector.registerFactory(LongTermMemoryManager.class, injector -> LongTermMemoryManager.getInstance());
        injector.registerFactory(ExperienceManager.class, injector -> ExperienceManager.getInstance());
        injector.registerFactory(ContentExtractor.class, injector -> ContentExtractor.getInstance());
        injector.registerFactory(FileIndexer.class, injector -> FileIndexer.getInstance());
        injector.registerFactory(MemorySearch.class, injector -> MemorySearch.getInstance());
        injector.registerFactory(MemoryManager.class, injector -> MemoryManager.getInstance());

        // 注册大脑组件
        injector.registerFactory(Hippocampus.class, injector -> Hippocampus.getInstance());
        injector.registerFactory(Prefrontal.class, injector -> Prefrontal.getInstance());

        // 注册工具管理器
        injector.registerFactory(ToolManagerRegistry.class, injector -> ToolManagerRegistry.getInstance());

        System.out.println("应用程序上下文初始化完成，注册了 " + injector.getRegisteredCount() + " 个组件");
    }

    /**
     * 获取组件实例
     */
    public <T> T getComponent(Class<T> clazz) {
        return injector.getInstance(clazz);
    }
}
