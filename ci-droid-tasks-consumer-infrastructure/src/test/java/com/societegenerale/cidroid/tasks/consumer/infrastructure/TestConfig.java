package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.CiDroidBehavior;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.NotifierMock;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.BestPracticeNotifierHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.NotificationsHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestCleaningHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestSizeCheckHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventMonitor;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.RebaseHandler;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

import static org.mockito.Mockito.mock;

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
    public GitHubMockServer gitHubMockServer() {
        return new GitHubMockServer();
    }

    @Bean
    public PushEventHandler rebaseHandler(Rebaser rebaser, SourceControlEventsReactionPerformer remoteSourceControl) {

        return new RebaseHandler(rebaser, remoteSourceControl);
    }

    @Bean
    public PushEventHandler notificationsHandler(SourceControlEventsReactionPerformer remoteSourceControl, NotifierMock notifierMock) {

        return new NotificationsHandler(remoteSourceControl, Collections.singletonList(notifierMock));
    }

    @Bean
    public PushEventHandler pullRequestCleaningHandler(SourceControlEventsReactionPerformer remoteSourceControl) {

        return new PullRequestCleaningHandler(remoteSourceControl, LocalDateTime::now, 180);
    }

    @Bean
    public PullRequestEventHandler bestPracticeNotifierHandler(List<Notifier> notifiers, SourceControlEventsReactionPerformer remoteSourceControl) {

        return new BestPracticeNotifierHandler(Collections.emptyMap(), notifiers, remoteSourceControl, new RestTemplateResourceFetcher());

    }

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.maxFilesInPRNotifier.enabled", havingValue = "true")
    @AutoConfigureOrder(2)
    public PullRequestEventHandler pullRequestSizeCheckHandler(CiDroidBehavior ciDroidBehavior,
                                                               List<Notifier> notifiers, SourceControlEventsReactionPerformer remoteSourceControl,
                                                               @Value("${cidroid-behavior.maxFilesInPRNotifier.maxFiles}") int maxFiles,
                                                               @Value("${cidroid-behavior.maxFilesInPRNotifier.warningMessage}") String warningMessage) {
        return new PullRequestSizeCheckHandler(notifiers, remoteSourceControl, maxFiles, warningMessage);

    }


    @Bean
    public MailSender mockMailSender() {

        return mock(MailSender.class);

    }

    @Bean
    public SourceControlBulkActionsPerformer mockSourceControlBulkActionsPerformer() {
        return mock(SourceControlBulkActionsPerformer.class);
    }

    @Bean
    public SourceControlEventsReactionPerformer mockSourceControlEventsReactionPerformer() {
        return mock(SourceControlEventsReactionPerformer.class);
    }

    @Bean
    public PushEventMonitor dummyPushEventMonitor() {

        // do nothing..
        return (pushEvent) -> {};
    }

}
