package com.ruoyi.project.camunda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.ruoyi.framework.web.controller.BaseController;

@Controller
public class ModelerController extends BaseController {

    @GetMapping("/camunda/designer")
    public String addModal() {
        return "camunda/designer";
    }

}
