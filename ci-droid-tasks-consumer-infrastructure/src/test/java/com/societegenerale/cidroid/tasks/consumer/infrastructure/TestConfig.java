package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.NotifierMock;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.RemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.*;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;
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
    public PushEventOnDefaultBranchHandler rebaseHandler(Rebaser rebaser, RemoteGitHub remoteGitHub){

        return new RebaseHandler(rebaser, remoteGitHub);
    }

    @Bean
    public PushEventOnDefaultBranchHandler notificationsHandler(RemoteGitHub remoteGitHub, NotifierMock notifierMock) {

        return new NotificationsHandler(remoteGitHub, Collections.singletonList(notifierMock));
    }

    @Bean
    public PushEventOnDefaultBranchHandler pullRequestCleaningHandler(RemoteGitHub remoteGitHub) {

        return new PullRequestCleaningHandler(remoteGitHub, LocalDateTime::now, 180);
    }

    @Bean
    public PullRequestEventHandler bestPracticeNotifierHandler(List<Notifier> notifiers, RemoteGitHub remoteGitHub) {

        return new BestPracticeNotifierHandler(Collections.emptyMap(), notifiers, remoteGitHub, new RestTemplateResourceFetcher()
                , Integer.MAX_VALUE, EMPTY);

    }

    @Bean
    public MailSender mockMailSender() {

        return mock(MailSender.class);

    }

}
