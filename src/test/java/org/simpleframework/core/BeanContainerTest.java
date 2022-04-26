package org.simpleframework.core;

import com.imooc.controller.MainPageController;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BeanContainerTest {

    private static BeanContainer beanContainer;

    @BeforeAll
    static void init() {
        beanContainer = BeanContainer.getInstance();
    }

    @Order(1)
    @Test
    public void loadBeansTest() {
        Assertions.assertEquals(false, beanContainer.isLoaded());
        beanContainer.loadBeans("com.imooc");
        Assertions.assertEquals(2, beanContainer.size());
        Assertions.assertEquals(true, beanContainer.isLoaded());
    }

    @Order(2)
    @Test
    public void getBeanTest() {
        MainPageController controller = (MainPageController) beanContainer.getBean(MainPageController.class);
        Assertions.assertEquals(true,controller instanceof MainPageController);
    }
}
