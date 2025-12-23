package com.springboot.JournalApp.controller;

import com.springboot.JournalApp.utils.JwtUtil;
import com.springboot.JournalApp.entity.User;
import com.springboot.JournalApp.service.UserDetailsServiceImpl;
import com.springboot.JournalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
@Slf4j
public class PublicController {

    @Autowired
    private UserService userService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    JwtUtil jwtutil;

    @GetMapping("health-check")
    public String healthCheck()
    {
        return "OK";
    }

    @PostMapping("/signup")
    public void signup(@RequestBody User user)
    {
        userService.saveNewEntry(user);
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user)
    {
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String jwt = jwtutil.generateToken(userDetails.getUsername());
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception during creating jwtAuthenticationToken");
            return new ResponseEntity<>("Incoorect suername or password", HttpStatus.BAD_REQUEST);
        }
    }

}
