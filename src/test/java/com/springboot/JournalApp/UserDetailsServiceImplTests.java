package com.springboot.JournalApp;

import com.springboot.JournalApp.repository.UserRepository;
import com.springboot.JournalApp.service.UserDetailsServiceImpl;
import lombok.Builder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

//import org.springframework.security.core.userdetails.User;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import com.springboot.JournalApp.entity.User;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTests {

    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Disabled
    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Disabled
    @Test
    void loadUserByUsernameTest(){
        when(userRepository.findByUsername(anyString()))
                .thenReturn(
                        User.builder()
                                .username("Abhi")
                                .password("123")
                                .roles(new ArrayList<>())
                                .build()
                );
        UserDetails user=userDetailsService.loadUserByUsername("Abhi");
        Assertions.assertNotNull(user);
    }
}
