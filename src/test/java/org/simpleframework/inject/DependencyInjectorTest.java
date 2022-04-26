package org.simpleframework.inject;

import com.imooc.controller.MainPageController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.simpleframework.core.BeanContainer;
import org.simpleframework.inject.annotation.DependencyInjector;

/**
 * @program: simpleframework
 * @description: 依赖注入测试
 * @author: zjk
 * @create: 2022-04-26 17:34
 **/
public class DependencyInjectorTest {

    @DisplayName("依赖注入 doIoc ")
    @Test
    public void doIocTest(){
        BeanContainer instance = BeanContainer.getInstance();
        instance.loadBeans("com.imooc");
        Assertions.assertEquals(true,instance.isLoaded());
        MainPageController mainPageController = (MainPageController)instance.getBean(MainPageController.class);
        Assertions.assertEquals(true,mainPageController instanceof MainPageController);
        Assertions.assertEquals(null,mainPageController.getHelloServlet());
        new DependencyInjector().DoIoc();
        Assertions.assertNotEquals(null,mainPageController.getHelloServlet());



    }

}
