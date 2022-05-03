package org.simpleframework.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * @program: simpleframework
 * @description: 创建动态代理对象并返回
 * @author: zjk
 * @create: 2022-05-03 15:38
 **/
public class ProxyCreator {

    /**
     * 创建动态代理对象
     *
     * @param targetClass       被代理的 Class 对象
     * @param methodInterceptor 方法拦截器
     * @return
     */
    public static Object createProxy(Class<?> targetClass, MethodInterceptor methodInterceptor) {
        return  Enhancer.create(targetClass, methodInterceptor);
    }
}
