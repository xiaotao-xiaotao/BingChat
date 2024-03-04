package com.xiaotao.bingchat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author：xiaotao
 * @date：2023/12/28 17:03
 */
@Controller
@RequestMapping
public class IndexController {

  @GetMapping("/index")
  public String index() {
    return "index";
  }
}
