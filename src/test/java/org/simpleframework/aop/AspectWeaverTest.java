package org.simpleframework.aop;

import com.imooc.controller.MainPageController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.simpleframework.core.BeanContainer;
import org.simpleframework.inject.annotation.DependencyInjector;

/**
 * @program: simpleframework
 * @description:
 * @author: zjk
 * @create: 2022-05-03 16:40
 **/
public class AspectWeaverTest {
    @DisplayName("织入通用逻辑测试：doAop()")
    @Test
    public void doAopTest(){
        BeanContainer beanContainer=BeanContainer.getInstance();
        beanContainer.loadBeans("com.imooc");
        new AspectWeaver().doAop();
        new DependencyInjector().DoIoc();
        MainPageController bean = (MainPageController)beanContainer.getBean(MainPageController.class);
        bean.a();
    }
}
