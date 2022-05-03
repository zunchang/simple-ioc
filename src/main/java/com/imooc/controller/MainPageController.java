package com.imooc.controller;

import com.imooc.HelloServlet;
import lombok.Getter;
import org.simpleframework.core.annotation.Controller;
import org.simpleframework.inject.annotation.Autowired;

@Controller
@Getter
public class MainPageController {
    @Autowired
    HelloServlet helloServlet;
    public void a(){
        System.out.println("执行方法");
    }
}
