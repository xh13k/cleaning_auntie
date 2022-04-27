package com.xh13k.cleaning_auntie;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * index controller
 *
 * @author makejava
 * {@code @date} 2022/04/27
 */
@Controller
public class IndexController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }

}
