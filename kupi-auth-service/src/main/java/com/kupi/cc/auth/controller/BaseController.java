package com.kupi.cc.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base Controller
 */
@RestController
public class BaseController {

    /**
     * index action
     *
     * @return
     */
    @GetMapping(path = "/", produces = "text/html")
    public String index() {
        return "<h1>Kupi Auth Service</h1>";
    }
}
