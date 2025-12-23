package com.springboot.JournalApp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    public <T> T get(String key, Class<T> entity)
    {
        try{
            Object o = redisTemplate.opsForValue().get(key);
            ObjectMapper objectMapper=new ObjectMapper();
            return objectMapper.readValue(o.toString() , entity);
        }
        catch (Exception e)
        {
            log.error("Error occured",e);
            return null;
        }
    }

    public void set(String key,Object o, long ttl)
    {
        try{
            ObjectMapper objectMapper=new ObjectMapper();
            String value=objectMapper.writeValueAsString(o);
            redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            log.error("Error occured",e);
        }
    }

}
