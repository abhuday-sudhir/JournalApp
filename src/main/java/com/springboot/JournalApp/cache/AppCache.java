package com.springboot.JournalApp.cache;


import com.springboot.JournalApp.entity.ConfigJournalAppEntity;
import com.springboot.JournalApp.repository.ConfigJournalAppRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AppCache {

    @Autowired
    ConfigJournalAppRepository configJournalAppRepository;

    public Map<String,String> cache;

    @PostConstruct
    public void init()
    {
        cache=new HashMap<>();
        List<ConfigJournalAppEntity> all=configJournalAppRepository.findAll();
        for(ConfigJournalAppEntity entity:all)
        {
            cache.put(entity.getKey(),entity.getValue());
        }
    }
}
