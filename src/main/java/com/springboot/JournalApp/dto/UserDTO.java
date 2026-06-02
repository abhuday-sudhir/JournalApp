package com.springboot.JournalApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO
{
    private String username;
    private String password;
    private boolean sentimentAnalysis;
    private String email;
}
