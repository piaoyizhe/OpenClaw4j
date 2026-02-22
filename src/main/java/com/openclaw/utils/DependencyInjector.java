package com.openclaw.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 依赖注入容器
 * 用于管理对象的创建和依赖关系
 */
public class DependencyInjector {
    private static DependencyInjector instance;
    private final Map<Class<?>, Object> singletonMap;
    private final Map<Class<?>, ObjectFactory<?>> factoryMap;

    /**
     * 对象工厂接口
     */
    public interface ObjectFactory<T> {
        T create(DependencyInjector injector);
    }

    /**
     * 私有构造方法
     */
    private DependencyInjector() {
        singletonMap = new HashMap<>();
        factoryMap = new HashMap<>();
    }

    /**
     * 获取单例实例
     */
    public static synchronized DependencyInjector getInstance() {
        if (instance == null) {
            instance = new DependencyInjector();
        }
        return instance;
    }

    /**
     * 注册单例对象
     */
    public <T> void registerSingleton(Class<T> clazz, T instance) {
        singletonMap.put(clazz, instance);
    }

    /**
     * 注册对象工厂
     */
    public <T> void registerFactory(Class<T> clazz, ObjectFactory<T> factory) {
        factoryMap.put(clazz, factory);
    }

    /**
     * 获取对象实例
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        // 先检查是否有单例实例
        if (singletonMap.containsKey(clazz)) {
            return (T) singletonMap.get(clazz);
        }

        // 再检查是否有工厂
        if (factoryMap.containsKey(clazz)) {
            ObjectFactory<T> factory = (ObjectFactory<T>) factoryMap.get(clazz);
            T instance = factory.create(this);
            // 默认将工厂创建的对象也作为单例缓存
            singletonMap.put(clazz, instance);
            return instance;
        }

        // 如果都没有，尝试直接实例化（要求有默认构造方法）
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            singletonMap.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("无法实例化对象: " + clazz.getName(), e);
        }
    }

    /**
     * 清除所有注册的对象
     */
    public void clear() {
        singletonMap.clear();
        factoryMap.clear();
    }

    /**
     * 获取注册的对象数量
     */
    public int getRegisteredCount() {
        return singletonMap.size() + factoryMap.size();
    }
}
