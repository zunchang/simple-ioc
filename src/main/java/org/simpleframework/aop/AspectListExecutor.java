package org.simpleframework.aop;

import lombok.Getter;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.simpleframework.aop.aspect.AspectInfo;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @program: simpleframework
 * @description: 用于向被代理对象的方法添加横切逻辑
 * @author: zjk
 * @create: 2022-05-03 11:36
 **/
public class AspectListExecutor implements MethodInterceptor {
    // 表示被代理的类
    private Class<?> targetClass;
    // 排序好的 Aspect 列表
    @Getter
    private List<AspectInfo> sortedAspectInfoList;

    public AspectListExecutor(Class<?> targetClass, List<AspectInfo> aspectInfoList) {
        this.targetClass = targetClass;
        this.sortedAspectInfoList = sortAspectInfoList(aspectInfoList);
    }

    /**
     * 依据 aspectInfoList 的 order 属性进行排序，确保 order 较小的 aspect 先被织入
     *
     * @param aspectInfoList
     * @return
     */
    private List<AspectInfo> sortAspectInfoList(List<AspectInfo> aspectInfoList) {
        Collections.sort(aspectInfoList, Comparator.comparingInt(AspectInfo::getOrderIndex));
        return aspectInfoList;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Object returnValue = null;
        // 1. 按照order的顺序升序执行完所有Aspect的before方法
        invokeBeforeAdvices(method, args);
        try {
            // 2. 执行被代理类的方法
            returnValue = methodProxy.invokeSuper(proxy, args);
            // 3. 如果被代理方法正常返回，则按照order的顺序降序执行完所有的 Aspect 的afterReturning方法
            returnValue = invokeAfterReturningAdvices(method, args, returnValue);
        } catch (Exception e) {
            // 4. 如果被代理方法抛出异常，则按照order的顺序降序执行完所有 Aspect 的afterThrowing方法
            invokeAfterThrowingAdives(method, args, e);
        }
        return returnValue;
    }

    /**
     * 如果被代理方法抛出异常，则按照order的顺序降序执行完所有 Aspect 的afterThrowing方法
     * @param method
     * @param args
     * @param e
     */
    private void invokeAfterThrowingAdives(Method method, Object[] args, Exception e) throws Throwable {
        for (int i = sortedAspectInfoList.size() - 1; i >= 0; i--) {
            sortedAspectInfoList.get(i).getAspectObject().afterThrowing(targetClass, method, args, e);
        }
    }

    /**
     * 如果被代理方法正常返回，则按照order的顺序降序执行完所有的 Aspect 的afterReturning方法
     *
     * @param method      被代理方法
     * @param args        参数
     * @param returnValue 被代理方法返回值
     * @return
     */
    private Object invokeAfterReturningAdvices(Method method, Object[] args, Object returnValue) throws Throwable {
        Object result = null;
        for (int i = sortedAspectInfoList.size() - 1; i >= 0; i--) {
            result = sortedAspectInfoList.get(i).getAspectObject().afterReturning(targetClass, method, args, returnValue);
        }
        return result;
    }

    /**
     * 按照order的顺序升序执行完所有Aspect的before方法
     *
     * @param method
     * @param args
     */
    private void invokeBeforeAdvices(Method method, Object[] args) throws Throwable {
        for (AspectInfo aspectInfo : sortedAspectInfoList) {
            aspectInfo.getAspectObject().before(targetClass, method, args);
        }
    }
}
