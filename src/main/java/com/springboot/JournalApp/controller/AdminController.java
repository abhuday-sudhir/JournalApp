package com.springboot.JournalApp.controller;

import com.springboot.JournalApp.entity.User;
import com.springboot.JournalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController
{
    @Autowired
    UserService userService;

    @GetMapping("/get-users")
    public ResponseEntity<?> getAll()
    {
        List<User> all=userService.getAll();
        if(all != null && !all.isEmpty())
        {
            return new ResponseEntity<>(all , HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create-admin-users")
    public void createUser(@RequestBody User user)
    {
        userService.saveAdmin(user);
    }
}
