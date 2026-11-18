package com.kupi.cc.location.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base Controller
 */
@CrossOrigin
@RestController
public class BaseController {

    /**
     * Index
     *
     * @return text/html
     */
    @GetMapping(path = "/", produces = "text/html")
    public String index() {
        return "<h1>Kupi Location Service/h1>";
    }

}
