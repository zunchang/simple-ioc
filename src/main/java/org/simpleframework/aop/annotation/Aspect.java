package org.simpleframework.aop.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
    // 表示当前被 Aspect 标记的横切逻辑，被织入类的位置
    String pointcut();
}
