package org.simpleframework.inject.annotation;

import lombok.extern.slf4j.Slf4j;
import org.simpleframework.core.BeanContainer;
import org.simpleframework.util.ClassUtil;
import org.simpleframework.util.ValidationUtil;

import java.lang.reflect.Field;
import java.util.Set;

@Slf4j
public class DependencyInjector {
    /**
     * bean容器
     */
    private BeanContainer beanContainer;

    public DependencyInjector() {
        beanContainer = BeanContainer.getInstance();
    }

    /**
     * 执行IOC
     */
    public void DoIoc() {
        if (ValidationUtil.isEmpty(beanContainer.getClasses())) {
            log.warn("BeanContainer为空");
            return;
        }
        // 1. 遍历Bean容器中所有的Class对象
        for (Class<?> clazz : beanContainer.getClasses()) {
            // 2. 遍历Class对象的所有的成员变量
            Field[] fields = clazz.getDeclaredFields();
            if (ValidationUtil.isEmpty(fields)) {
                continue;
            }
            for (Field field : fields) {
                // 3. 找出被Autowired标记的成员变量
                if (field.isAnnotationPresent(Autowired.class)) {
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    String autowiredValue = autowired.value();
                    // 4. 获取这些成员变量的类型
                    Class<?> fieldClass = field.getType();
                    // 5. 获取这些成员变量的类型在容器里对应的实例
                    Object fieldValue = getFiledInstance(fieldClass, autowiredValue);
                    if (fieldValue == null) {
                        throw new RuntimeException("无法获取成员变量类型对应的实例" + fieldClass.getName());
                    } else {
                        // 6. 通过反射将对应的成员变量实例注入到成员变量所在的类的实例里
                        Object targetBean = beanContainer.getBean(clazz);
                        ClassUtil.setField(field, targetBean, fieldValue, true);
                    }
                }

            }

        }
    }

    /**
     * 根据Class在beanContainer里获取其实例或者实现类
     *
     * @param fieldClass
     * @param autowiredValue
     * @return
     */
    private Object getFiledInstance(Class<?> fieldClass, String autowiredValue) {
        Object fieldValue = beanContainer.getBean(fieldClass);
        if (fieldValue != null) {
            return fieldValue;
        } else {
            Class<?> implementClass = getImplementedClass(fieldClass, autowiredValue);
            if (implementClass != null) {
                return beanContainer.getBean(implementClass);
            } else {
                return null;
            }
        }
    }

    /**
     * 获取接口的实现类
     */
    private Class<?> getImplementedClass(Class<?> fieldClass, String autowiredValue) {
        Set<Class<?>> classSet = beanContainer.getClassesBySuper(fieldClass);
        if (!ValidationUtil.isEmpty(classSet)) {
            if (ValidationUtil.isEmpty(autowiredValue)) {
                if (classSet.size() == 1) {
                    return classSet.iterator().next();
                } else {
                    throw new RuntimeException(fieldClass.getName() + "具有多个实现类,需要指定");
                }
            } else {
                for (Class<?> clazz : classSet) {
                    if (autowiredValue.equals(clazz.getSimpleName())) {
                        return clazz;
                    }
                }
            }
        }
        return null;
    }

}
