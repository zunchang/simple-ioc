package org.simpleframework.aop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.simpleframework.aop.aspect.AspectInfo;
import org.simpleframework.aop.mock.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: simpleframework
 * @description:
 * @author: zjk
 * @create: 2022-05-03 12:07
 **/
public class AspectListExecutorTest {

    @DisplayName("Aspect 排序")
    @Test
    public void sortTest() {
        List<AspectInfo> aspectInfoList = new ArrayList<>();
        aspectInfoList.add(new AspectInfo(3, new Mock1(),null));
        aspectInfoList.add(new AspectInfo(5, new Mock2(),null));
        aspectInfoList.add(new AspectInfo(2, new Mock3(),null));
        aspectInfoList.add(new AspectInfo(4, new Mock4(),null));
        aspectInfoList.add(new AspectInfo(1, new Mock5(),null));
        AspectListExecutor aspectListExecutor = new AspectListExecutor(AspectListExecutor.class, aspectInfoList);
        List<AspectInfo> sortedAspectInfoList = aspectListExecutor.getSortedAspectInfoList();
        for (AspectInfo a:sortedAspectInfoList) {
            System.out.println(a.getAspectObject().getClass().getName());
        }
    }
}
