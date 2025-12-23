package com.springboot.JournalApp.controller;

import com.springboot.JournalApp.api.response.WeatherResponse;
import com.springboot.JournalApp.entity.User;
import com.springboot.JournalApp.repository.UserRepository;
import com.springboot.JournalApp.service.UserService;
import com.springboot.JournalApp.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeatherService weatherService;

    @PutMapping()
    public ResponseEntity<User> updateUser(@RequestBody User user)
    {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String username= authentication.getName();
        User old_user=userService.findByUsername(username);
        if(old_user != null)
        {
            old_user.setUsername(user.getUsername());
            old_user.setPassword(user.getPassword());
            userService.saveNewEntry(old_user);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping()
    public ResponseEntity<User> updateUser()
    {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String username= authentication.getName();
        userRepository.deleteByUsername(username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<?> greeting()
    {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        WeatherResponse reponse = weatherService.getWeather("Delhi");
        String greeting="";
        if(reponse!=null)
        {
            greeting="Hi "+authentication.getName()+" weather feels like "+reponse.getCurrent().getFeelsLike();
        }
        return new ResponseEntity<>(greeting, HttpStatus.OK);
    }

}
