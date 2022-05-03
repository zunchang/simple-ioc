package org.simpleframework.aop;

import org.simpleframework.aop.annotation.Aspect;
import org.simpleframework.aop.annotation.Order;
import org.simpleframework.aop.aspect.AspectInfo;
import org.simpleframework.aop.aspect.DefaultAspect;
import org.simpleframework.core.BeanContainer;
import org.simpleframework.util.ValidationUtil;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @program: simpleframework
 * @description:
 * @author: zjk
 * @create: 2022-05-03 15:43
 **/
public class AspectWeaver {
    private BeanContainer beanContainer;

    public AspectWeaver() {
        this.beanContainer = BeanContainer.getInstance();
    }

    public void doAop() {
        // 1. 获取所有的切面类
        Set<Class<?>> aspectSet = beanContainer.getClassesByAnnotation(Aspect.class);
        // 2. 将切面类按照不同的织入目标进行切分
        Map<Class<? extends Annotation>, List<AspectInfo>> categorizedMap = new HashMap<>();
        if (ValidationUtil.isEmpty(aspectSet)) return;
        for (Class<?> aspectClass : aspectSet) {
            if (verifyAspect(aspectClass)) {
                categorizeAspect(categorizedMap, aspectClass);
            } else {
                throw new RuntimeException("有可能不满足 AspectClass 规范要求(1. 框架中一定要遵循给Aspect类添加 @Aspect和 @Order的规范，同时必须继承自DefaultAspect.class\n" +
                        "2. @Aspect 的属性值不能是他本身)");
            }
        }
        // 3. 按照不同的织入目标分别按序织入Aspect逻辑
        if (ValidationUtil.isEmpty(categorizedMap)) return;
        for (Class<? extends Annotation> category : categorizedMap.keySet()) {
            weaveByCategory(category, categorizedMap.get(category));
        }
    }

    /**
     * 进行逻辑织入
     *
     * @param category
     * @param aspectInfos
     */
    private void weaveByCategory(Class<? extends Annotation> category, List<AspectInfo> aspectInfos) {
        // 1. 获取被代理类的集合
        Set<Class<?>> classSet = beanContainer.getClassesByAnnotation(category);
        if (ValidationUtil.isEmpty(classSet)) return;
        // 2. 遍历被代理类，分别为每个代理类生成动态代理实例
        for (Class<?> targetClass : classSet) {
            AspectListExecutor aspectListExecutor = new AspectListExecutor(targetClass, aspectInfos);
            Object proxyBean = ProxyCreator.createProxy(targetClass, aspectListExecutor);
            // 3. 将动态代理对象实例添加到容器里，取代未被代理前的类实例
            beanContainer.addBean(targetClass, proxyBean);
        }

    }

    /**
     * 将切面类按照不同的织入目标进行切分
     *
     * @param categorizedMap
     * @param aspectClass
     */
    private void categorizeAspect(Map<Class<? extends Annotation>, List<AspectInfo>> categorizedMap, Class<?> aspectClass) {
        Order orderTag = aspectClass.getAnnotation(Order.class);
        Aspect aspectTag = aspectClass.getAnnotation(Aspect.class);
        DefaultAspect aspect = (DefaultAspect) beanContainer.getBean(aspectClass);
        AspectInfo aspectInfo = new AspectInfo(orderTag.value(), aspect);
        if (!categorizedMap.containsKey(aspectTag.value())) {
            // 如果织入的 Joinpoint 第一次出现，则以该 Joinpoint 为key，以新创建的 List<AspectInfo>为value
            List<AspectInfo> aspectInfoList = new ArrayList<>();
            aspectInfoList.add(aspectInfo);
            categorizedMap.put(aspectTag.value(), aspectInfoList);
        } else {
            categorizedMap.get(aspectTag.value()).add(aspectInfo);
        }

    }

    /**
     * 验证aspectClass是否满足框架的aspectClass要求
     * 1. 框架中一定要遵循给Aspect类添加 @Aspect和 @Order的规范，同时必须继承自DefaultAspect.class
     * 2. @Aspect 的属性值不能是他本身
     *
     * @param aspectClass
     * @return
     */
    private boolean verifyAspect(Class<?> aspectClass) {
        return aspectClass.isAnnotationPresent(Aspect.class) &&
                aspectClass.isAnnotationPresent(Order.class) &&
                DefaultAspect.class.isAssignableFrom(aspectClass) &&
                aspectClass.getAnnotation(Aspect.class).value() != Aspect.class;
    }
}
