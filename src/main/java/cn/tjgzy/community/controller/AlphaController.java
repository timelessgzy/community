package cn.tjgzy.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author GongZheyi
 * @create 2021-09-28-21:51
 */
@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @GetMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello!";
    }
}
