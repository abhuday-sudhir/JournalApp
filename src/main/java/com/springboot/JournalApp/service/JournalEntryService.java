package com.springboot.JournalApp.service;

import com.springboot.JournalApp.entity.JournalEntry;
import com.springboot.JournalApp.entity.User;
import com.springboot.JournalApp.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class JournalEntryService {
   @Autowired
   private JournalEntryRepository journalEntryRepository;

   @Autowired
   private  UserService userService;

   private static final Logger logger=LoggerFactory.getLogger(JournalEntryService.class);

   @Transactional
   public void saveEntry(JournalEntry journalEntry, String username)
   {
       try {
           User user=userService.findByUsername(username);
           journalEntry.setDate(LocalDateTime.now());
           JournalEntry saved = journalEntryRepository.save(journalEntry);
           user.getJournalEntries().add(saved);
           userService.saveEntry(user);
       } catch (Exception e) {
           logger.info(e.getMessage());
           throw new RuntimeException("An error occurred while saving the entry");
       }
   }

    public void saveEntry(JournalEntry journalEntry)
    {
        journalEntry.setDate(LocalDateTime.now());
        journalEntryRepository.save(journalEntry);
    }

   public List<JournalEntry> getAll()
   {
       return journalEntryRepository.findAll();
   }

   public Optional<JournalEntry> findById(ObjectId id)
   {
       return journalEntryRepository.findById(id);
   }

   @Transactional
   public boolean deleteById(ObjectId id, String username)
   {
       boolean removed=false;
       try {
           User user=userService.findByUsername(username);
           removed=user.getJournalEntries().removeIf(x -> x.getId().equals(id));
           if(removed)
           {
               userService.saveEntry(user);
               journalEntryRepository.deleteById(id);
           }
       } catch (Exception e) {
           System.out.println(e);
           throw new RuntimeException("An error occured while deleting the entry");
       }
       return removed;
   }
}
