package com.societegenerale.cidroid.tasks.consumer.autoconfigure;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.GitRebaser;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.GitWrapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.IncomingGitHubEvent;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.RestTemplateResourceFetcher;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.CiDroidBehavior;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers.EMailNotifier;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers.GitHubPullRequestCommentNotifier;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers.HttpNotifier;
import com.societegenerale.cidroid.tasks.consumer.services.RemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.*;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSender;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableBinding(IncomingGitHubEvent.class)
@Import({InfraConfig.class})
public class CiDroidTasksConsumerAutoConfiguration {

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.notifyOwnerForNonMergeablePr.enabled", havingValue = "true")
    @AutoConfigureOrder(1)
    public PushEventOnDefaultBranchHandler notificationHandler(RemoteGitHub gitHub, List<Notifier> notifiers) {
        return new NotificationsHandler(gitHub, notifiers);
    }

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.closeOldPullRequests.enabled", havingValue = "true")
    @AutoConfigureOrder(2)
    public PushEventOnDefaultBranchHandler pullRequestCleaningHandler(RemoteGitHub gitHub,
                                                                      @Value("${cidroid-behavior.closeOldPullRequests.limitInDays}") int prAgeLimitInDays) {
        return new PullRequestCleaningHandler(gitHub, LocalDateTime::now, prAgeLimitInDays);
    }

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.tryToRebaseOpenPrs.enabled", havingValue = "true")
    @AutoConfigureOrder(3)
    public PushEventOnDefaultBranchHandler rebaseHandler(RemoteGitHub gitHub, @Value("${gitHub.login}") String gitLogin,
                                                         @Value("${gitHub.password}") String gitPassword) {

        return new RebaseHandler(new GitRebaser(gitLogin, gitPassword, new GitWrapper()), gitHub);
    }

    @Bean
    @ConditionalOnMissingBean(PushEventOnDefaultBranchHandler.class)
    @AutoConfigureOrder(500)
    public PushEventOnDefaultBranchHandler dummyPushEventOnDefaultBranchHandler() {

        return new DummyPushEventOnDefaultBranchHandler();
    }

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.bestPracticeNotifier.enabled", havingValue = "true")
    @AutoConfigureOrder(1)
    public PullRequestEventHandler bestPracticeNotifierHandler(CiDroidBehavior ciDroidBehavior, List<Notifier> notifiers,
                                                               RemoteGitHub remoteGitHub) {

        return new BestPracticeNotifierHandler(ciDroidBehavior.getPatternToResourceMapping(), notifiers, remoteGitHub,
                new RestTemplateResourceFetcher());

    }

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.maxFilesInPRNotifier.enabled", havingValue = "true")
    @AutoConfigureOrder(2)
    public PullRequestEventHandler pullRequestSizeCheckHandler(CiDroidBehavior ciDroidBehavior,
                                                               List<Notifier> notifiers, RemoteGitHub remoteGitHub) {

        return new PullRequestSizeCheckHandler(notifiers, remoteGitHub, new RestTemplateResourceFetcher(),
                ciDroidBehavior.getMaxFilesInPr(), ciDroidBehavior.getMaxFilesInPRExceededWarningMessage());

    }

    @Bean
    @ConditionalOnMissingBean(PullRequestEventHandler.class)
    @AutoConfigureOrder(500)
    public PullRequestEventHandler dummyPullRequestEventHandler() {

        return new DummyPullRequestEventHandler();
    }

    @Bean
    @ConditionalOnProperty(prefix = "notifiers", value = "github.prComment.enable", havingValue = "true")
    public Notifier gitHubCommentOnPRnotifier(RemoteGitHub gitHub) {

        return new GitHubPullRequestCommentNotifier(gitHub);
    }

    @Bean
    @ConditionalOnProperty(prefix = "notifiers", value = "email.enable", havingValue = "true")
    public Notifier emailNotifier(MailSender javaMailSender, @Value("${spring.mail.sender}") String mailSender) {

        return new EMailNotifier(javaMailSender, mailSender);
    }

    @Bean
    @ConditionalOnProperty(prefix = "notifiers", value = "http.targetUrl")
    public Notifier httpNotifier(@Value("${notifiers.http.targetUrl}") String targetUrl) {

        return new HttpNotifier(targetUrl);
    }

}