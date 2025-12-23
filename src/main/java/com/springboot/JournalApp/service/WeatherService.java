package com.springboot.JournalApp.service;

import com.springboot.JournalApp.api.response.WeatherResponse;
import com.springboot.JournalApp.cache.AppCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherService {

    @Value("${weather.api.key}")
    private String API_KEY;

    @Autowired
    AppCache appCache;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    RedisService redisService;

    public  WeatherResponse getWeather(String city)
    {
        WeatherResponse weatherResponse = redisService.get("weather_of_"+city, WeatherResponse.class);
        if(weatherResponse!=null)
        {
            return weatherResponse;
        }
        else
        {
            String api=appCache.cache.get("weather_api").replace("<API_KEY>",API_KEY).replace("<CITY>",city);
            ResponseEntity<WeatherResponse> exchange = restTemplate.exchange(api, HttpMethod.GET, null, WeatherResponse.class);
            WeatherResponse body = exchange.getBody();
            redisService.set("weather_of_"+city, body,600 );
            return body;
        }
    }
}
