package com.factory.controller;

import com.factory.common.api.RestApiEndpoints;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RestApiEndpoints.DATA)
public class DataController {

    @GetMapping(value = "/test")
    public String test() {
        return "Hello world";
    }
}
