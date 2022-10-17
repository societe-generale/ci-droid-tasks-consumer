package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.mocks.BitBucketMockServer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.mocks.NotifierMock;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.*;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

import java.util.List;

import static org.mockito.Mockito.mock;

// Todo test rebaseHandler, NotificationsHandler, pullRequestCleaningHandler
@Configuration
@EnableAutoConfiguration
public class TestConfig {

    @Bean
    public Rebaser mockRebaser() {

        return mock(Rebaser.class);
    }

    @Bean
    public NotifierMock mockNotifier() {
        return new NotifierMock();
    }

    @Bean
    public BitBucketMockServer bitBucketMockServer() {
        return new BitBucketMockServer();
    }

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.maxFilesInPRNotifier.enabled", havingValue = "true")
    @AutoConfigureOrder(2)
    public PullRequestEventHandler pullRequestSizeCheckHandler(List<Notifier> notifiers, SourceControlEventsReactionPerformer remoteSourceControl,
                                                               @Value("${cidroid-behavior.maxFilesInPRNotifier.maxFiles}") int maxFiles,
                                                               @Value("${cidroid-behavior.maxFilesInPRNotifier.warningMessage}") String warningMessage) {
        return new PullRequestSizeCheckHandler(notifiers, remoteSourceControl, maxFiles, warningMessage);

    }

    @Bean
    public MailSender mockMailSender() {
        return mock(MailSender.class);
    }

    @Bean
    public PushEventMonitor dummyPushEventMonitor() {
        // do nothing..
        return (pushEvent) -> {};
    }

}
