package com.springboot.JournalApp;

import com.springboot.JournalApp.scheduler.UserScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserSchedulerTests {

    @Autowired
    private UserScheduler userScheduler;

    @Test
    public void fetchUsersAndSendEmail()
    {
        userScheduler.fetchUsersAndSendEmail();
    }
}
