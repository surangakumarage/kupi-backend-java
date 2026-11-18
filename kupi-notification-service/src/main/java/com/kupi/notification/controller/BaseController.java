package com.kupi.notification.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base Controller
 */
@RestController
public class BaseController {

    /**
     * Index
     * @return text/html
     */
    @GetMapping(path = "/", produces = "text/html")
    public String index() {
        return "<h1>Kupi Notification Service</h1>";
    }

}
