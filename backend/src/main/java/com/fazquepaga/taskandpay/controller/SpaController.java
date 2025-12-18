package com.fazquepaga.taskandpay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to forward all non-API and non-static resource requests to
 * index.html.
 * This is necessary for Client-Side Routing (SPA) to work.
 */
@Controller
public class SpaController {

    @RequestMapping(value = {
            "/",
            "/login",
            "/register",
            "/add-child",
            "/dashboard",
            "/gift-cards",
            "/child-login",
            "/child-portal",
            "/child/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
