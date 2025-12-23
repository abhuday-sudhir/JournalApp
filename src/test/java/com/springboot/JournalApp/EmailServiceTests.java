package com.springboot.JournalApp;

import com.springboot.JournalApp.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTests {

    @Autowired
    EmailService emailService;

    @Test
    void checkSendMail()
    {
        emailService.sendMail("abhuday11sudhir@gmail.com", "xyx","Hi");
    }

}
