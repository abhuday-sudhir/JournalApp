package com.springboot.JournalApp.scheduler;

import com.springboot.JournalApp.cache.AppCache;
import com.springboot.JournalApp.entity.JournalEntry;
import com.springboot.JournalApp.entity.User;
import com.springboot.JournalApp.enums.Sentiment;
import com.springboot.JournalApp.model.SentimentData;
import com.springboot.JournalApp.repository.UserRepositoryImpl;
import com.springboot.JournalApp.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserScheduler {

    @Autowired
    EmailService emailService;

    @Autowired
    UserRepositoryImpl userRepository;

    @Autowired
    AppCache appCache;

    @Autowired
    private KafkaTemplate<String,SentimentData> kafkaTemplate;

    @Scheduled(cron = "0 0 9 ? * SUN")
    public void fetchUsersAndSendEmail()
    {
        List<User> usersForSA = userRepository.getUsersForSA();
        for(User user:usersForSA)
        {
            List<JournalEntry> journalEntries = user.getJournalEntries();
            List<Sentiment> sentiments = journalEntries.stream().map(x -> x.getSentiment()).collect(Collectors.toList());
            Map<Sentiment , Integer> sentimentCounts=new HashMap<>();
            for(Sentiment sentiment:sentiments)
            {
                sentimentCounts.put(sentiment , sentimentCounts.getOrDefault(sentiment, 0)+1);
            }
            Sentiment mostFrequentSentiment=null;
            int maxCount=0;
            for(Map.Entry<Sentiment,Integer> entry : sentimentCounts.entrySet())
            {
                if(entry.getValue() > maxCount)
                {
                    maxCount=entry.getValue();
                    mostFrequentSentiment=entry.getKey();
                }
            }
            if(mostFrequentSentiment!=null)
            {
                SentimentData sentimentData=SentimentData.builder().email(user.getEmail()).sentiment("Most Frequent sentiment fro 7 days"+mostFrequentSentiment).build();
                kafkaTemplate.send("weekly_sentiments" , sentimentData.getEmail() , sentimentData);
//                emailService.sendMail(user.getEmail(), "Sentiment for last 7 days" , mostFrequent.toString());
            }
        }
    }

    @Scheduled(cron="0 0/5 * 1/1 * ? ")
    public void clearAppCache()
    {
        appCache.init();
    }

}
