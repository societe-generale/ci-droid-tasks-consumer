package com.societegenerale.cidroid.tasks.consumer;

import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventMonitor;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

import static org.mockito.Mockito.mock;

@Configuration
public class TestConfig {

    @Bean
    public Rebaser mockRebaser() {

        return mock(Rebaser.class);
    }

    @Bean
    public Notifier mockNotifier() {
        return mock(Notifier.class);
    }

    @Bean
    public MailSender mockMailSender() {
        return mock(MailSender.class);
    }

    @Bean
    public PushEventMonitor dummyPushEventMonitor() {
        return (pushEvent) -> {};
    }

}
