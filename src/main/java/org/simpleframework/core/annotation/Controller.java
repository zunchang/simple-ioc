package org.simpleframework.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 表示作用在类上
@Target(ElementType.TYPE)
// 可通过反射获取
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

}
