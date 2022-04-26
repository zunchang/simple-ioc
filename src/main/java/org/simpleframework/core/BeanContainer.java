package org.simpleframework.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.core.annotation.Component;
import org.simpleframework.core.annotation.Controller;
import org.simpleframework.core.annotation.Repository;
import org.simpleframework.core.annotation.Service;
import org.simpleframework.util.ClassUtil;
import org.simpleframework.util.ValidationUtil;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanContainer {
    /**
     * 存放所有被配置标记的目标对象的Map
     */
    private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap();

    /**
     * 加载bean的注解列表
     */
    private static final List<Class<? extends Annotation>> BEAN_ANNOTATION
            = Arrays.asList(Component.class, Controller.class, Repository.class, Service.class);


    /**
     * 获取Bean容器的实例
     * 采用基于枚举的饿汉单例模式，可以防止反射或者反序列化的侵入，保证线程的安全
     *
     * @return
     */
    public static BeanContainer getInstance() {
        return ContainerHolder.HOLDER.instance;
    }

    private enum ContainerHolder {
        HOLDER;
        private BeanContainer instance;

        ContainerHolder() {
            instance = new BeanContainer();
        }
    }

    /**
     * 容器是否加载过 bean
     */
    private boolean loaded = false;

    /**
     * 是否加载过 bean
     *
     * @return
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Bean实例数量
     *
     * @return
     */

    public int size() {
        return beanMap.size();
    }

    /**
     * 扫描加载所有 Bean
     *
     * @param packageName 包名
     */
    public synchronized void loadBeans(String packageName) {
        //判断bean容器是否被加载过
        if (isLoaded()) {
            log.warn("bean容器已经被加载过");
            return;
        }
        // 获取 package 下所有 Class 对象
        Set<Class<?>> classSet = ClassUtil.extractPackageClass(packageName);
        if (ValidationUtil.isEmpty(classSet)) {
            log.warn("包为空" + packageName);
            return;
        }
        for (Class<?> clazz : classSet) {
            for (Class<? extends Annotation> annotation : BEAN_ANNOTATION) {
                // 类上方是否定义注解
                if (clazz.isAnnotationPresent(annotation)) {
                    // 将目标类本身作为键，目标类的实例作为值，放到beanMap中
                    beanMap.put(clazz, ClassUtil.newInstance(clazz, true));
                }
            }
        }
        loaded = true;
    }

    /**
     * 添加一个class对象及其Bean实例
     *
     * @param clazz class对象
     * @param bean  bean实例
     * @return
     */
    public Object addBean(Class<?> clazz, Object bean) {
        return beanMap.put(clazz, bean);
    }

    /**
     * 移除一个IOC容器管理的对象
     *
     * @param clazz Class对象
     * @return 删除的Bean实例，没有则返回null
     */
    public Object removeBean(Class<?> clazz) {
        return beanMap.remove(clazz);
    }

    /**
     * 根据Class对象获取Bean实例
     *
     * @param clazz Class对象
     * @return Bean实例
     */
    public Object getBean(Class<?> clazz) {
        return beanMap.get(clazz);
    }

    /**
     * 获取容器管理的所有Class对象集合
     *
     * @return
     */
    public Set<Class<?>> getClasses() {
        return beanMap.keySet();
    }

    /**
     * 获取所有Bean集合
     *
     * @return Bean集合
     */
    public Set<Object> getBeans() {
        return new HashSet<>(beanMap.values());
    }

    /**
     * 根据注解筛选出Bean的Class集合
     *
     * @param annotation 注解
     * @return class集合
     */
    public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        //1 获取beanMap的所有的Class对象
        Set<Class<?>> ketSet = getClasses();
        if (ValidationUtil.isEmpty(ketSet)) {
            log.warn("beanMap为空");
        }
        //2 通过注解筛选被标记的class对象，并添加到classSet里
        Set<Class<?>> classSet = new HashSet<>();
        for (Class<?> clazz : ketSet) {
            // 判断是否有相关注解标记
            if (clazz.isAnnotationPresent(annotation)) {
                classSet.add(clazz);
            }
        }
        return classSet.size() > 0 ? classSet : null;
    }

    /**
     * 通过接口或者父类获取实现类或者子类的Class集合,不包括其本身
     *
     * @param interfaceOrClass 接口class或者父类class
     * @return
     */

    public Set<Class<?>> getClassesBySuper(Class<?> interfaceOrClass) {
        //1 获取beanMap的所有的Class对象
        Set<Class<?>> ketSet = getClasses();
        if (ValidationUtil.isEmpty(ketSet)) {
            log.warn("beanMap为空");
        }
        //2 判断keySet里的元素是否是传入的接口或者类的子类，并添加到classSet里
        Set<Class<?>> classSet = new HashSet<>();
        for (Class<?> clazz : ketSet) {
            // 判断 clazz 是否是 interfaceOrClass 的子类
            if (interfaceOrClass.isAssignableFrom(clazz) && !clazz.equals(interfaceOrClass)) {
                classSet.add(clazz);
            }
        }
        return classSet.size() > 0 ? classSet : null;
    }

}
