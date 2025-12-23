package com.springboot.JournalApp.service;

import com.springboot.JournalApp.repository.UserRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    JavaMailSender javaMailSender;

    public void sendMail(String to , String subject, String body)
    {
        try {
            SimpleMailMessage mailMessage=new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(body);
            javaMailSender.send(mailMessage);
        }
        catch (Exception e)
        {
            log.error("Exception occured while sending mail" , e);
        }
    }
}
