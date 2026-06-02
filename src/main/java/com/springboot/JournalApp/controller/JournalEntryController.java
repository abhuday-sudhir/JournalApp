package com.springboot.JournalApp.controller;

import com.springboot.JournalApp.entity.JournalEntry;
import com.springboot.JournalApp.entity.User;
import com.springboot.JournalApp.service.JournalEntryService;
import com.springboot.JournalApp.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/journal")
@Tag(name="Journal APIs")
public class JournalEntryController {
    private static final Pattern OID_PATTERN = Pattern.compile("\"\\$oid\"\\s*:\\s*\"([a-fA-F0-9]{24})\"");

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    @GetMapping()
    public ResponseEntity<?> getAllJournalEntriesOfUser()
    {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        User user = userService.findByUsername(username);
        List<JournalEntry> all=user.getJournalEntries();
        if(all!=null && !all.isEmpty())
        {
            return new ResponseEntity<>(all,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @PostMapping()
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry myentry)
    {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();;
        try {
            myentry.setDate(LocalDateTime.now());
            journalEntryService.saveEntry(myentry, username);
            return new ResponseEntity<>(myentry,HttpStatus.CREATED);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("id/{myid}")
    public ResponseEntity<?> getEntryById(@PathVariable String myid)
    {
        ObjectId entryId = parseObjectId(myid);
        if (entryId == null) {
            return new ResponseEntity<>("Invalid journal entry identifier", HttpStatus.BAD_REQUEST);
        }
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String username= authentication.getName();
        User user = userService.findByUsername(username);
        List<JournalEntry> collect = user.getJournalEntries().stream().filter(x -> x.getId().equals(entryId)).collect(Collectors.toList());
        if(!collect.isEmpty())
        {
            Optional<JournalEntry> journalEntry=journalEntryService.findById(entryId);
            if(journalEntry.isPresent())
            {
                return new ResponseEntity<>(journalEntry.get(),HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("id/{myid}")
    public ResponseEntity<?> deleteEntryById(@PathVariable String myid)
    {
        ObjectId entryId = parseObjectId(myid);
        if (entryId == null) {
            return new ResponseEntity<>("Invalid journal entry identifier", HttpStatus.BAD_REQUEST);
        }
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        boolean removed = journalEntryService.deleteById(entryId, username);
        if(removed)
        {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("id/{myid}")
    public ResponseEntity<?> updateEntryById(@PathVariable String myid,  @RequestBody JournalEntry newEntry)
    {
        ObjectId entryId = parseObjectId(myid);
        if (entryId == null) {
            return new ResponseEntity<>("Invalid journal entry identifier", HttpStatus.BAD_REQUEST);
        }
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        User user = userService.findByUsername(username);
        List<JournalEntry> collect = user.getJournalEntries().stream().filter(x -> x.getId().equals(entryId)).collect(Collectors.toList());
        if(!collect.isEmpty())
        {
            Optional<JournalEntry> entry=journalEntryService.findById(entryId);
            if(entry.isPresent())
            {
                JournalEntry oldEntry=entry.get();
                oldEntry.setTitle(newEntry.getTitle()!=null && ! newEntry.getTitle().isEmpty() ? newEntry.getTitle() : oldEntry.getTitle());
                oldEntry.setContent(newEntry.getContent()!=null && ! newEntry.getContent().isEmpty() ? newEntry.getContent() : oldEntry.getContent());
                journalEntryService.saveEntry(oldEntry);
                return new ResponseEntity<>(oldEntry, HttpStatus.OK);
            }
        }

            return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private ObjectId parseObjectId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }

        // Accept both plain 24-char hex and JSON-like {"$oid":"..."} inputs.
        String normalized = rawId.trim();
        if (normalized.contains("$oid")) {
            Matcher matcher = OID_PATTERN.matcher(normalized);
            if (matcher.find()) {
                normalized = matcher.group(1);
            }
        }

        if (!ObjectId.isValid(normalized)) {
            return null;
        }
        return new ObjectId(normalized);
    }
}
