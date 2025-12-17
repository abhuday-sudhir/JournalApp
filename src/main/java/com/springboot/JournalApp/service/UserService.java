package com.springboot.JournalApp.service;

import com.springboot.JournalApp.entity.User;
import com.springboot.JournalApp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class UserService {
   @Autowired
   private UserRepository userRepository;

   private  static final PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();

    public boolean saveNewEntry(User user)
   {
       try {
           user.setPassword(passwordEncoder.encode(user.getPassword()));
           user.setRoles(Arrays.asList("USER"));
           userRepository.save(user);
           return true;
       }
       catch (Exception e)
       {
           log.error("Error occured for {} :",user.getUsername() , e);
           log.info("error");
           log.error("error");
           log.warn("error");
           log.debug("error");
           return false;
       }
   }

    public void saveAdmin(User user)
    {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER","ADMIN"));
        userRepository.save(user);
    }

   public void saveEntry(User user)
   {
       userRepository.save(user);
   }

   public  List<User> getAll()
   {
       return userRepository.findAll();
   }

   public Optional<User> getById(ObjectId id)
   {
       return userRepository.findById(id);
   }

   public  void deleteById(ObjectId id)
   {
       userRepository.deleteById(id);
   }

   public User findByUsername(String username)
   {
       return userRepository.findByUsername(username);
   }

}
