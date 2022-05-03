package org.simpleframework.aop.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
    // 表示当前被 Aspect 标记的横切逻辑，会织入到被属性逻辑标记的那些类中
    Class<? extends Annotation> value();
}
