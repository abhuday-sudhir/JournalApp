package com.springboot.JournalApp.controller;

import com.springboot.JournalApp.entity.User;
import com.springboot.JournalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserService userService;

    @PostMapping("/create-user")
    public void createNewUser(@RequestBody User user)
    {
        userService.saveNewEntry(user);
    }

    @GetMapping("health-check")
    public String healthCheck()
    {
        return "OK";
    }
}
