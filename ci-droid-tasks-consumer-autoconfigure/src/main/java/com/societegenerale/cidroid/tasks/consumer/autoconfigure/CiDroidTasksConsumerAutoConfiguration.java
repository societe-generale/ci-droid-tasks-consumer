package com.societegenerale.cidroid.tasks.consumer.autoconfigure;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.GitRebaser;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.GitWrapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.RestTemplateResourceFetcher;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.CiDroidBehavior;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.GitHubConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.GitLabConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers.EMailNotifier;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers.GitHubPullRequestCommentNotifier;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers.HttpNotifier;
import com.societegenerale.cidroid.tasks.consumer.services.RemoteSourceControl;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.BestPracticeNotifierHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.DummyPullRequestEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.DummyPushEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.NotificationsHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestCleaningHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestSizeCheckHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventMonitor;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.RebaseHandler;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSender;

@Configuration
@Import({InfraConfig.class, GitHubConfig.class, GitLabConfig.class})
public class CiDroidTasksConsumerAutoConfiguration {

    private static final PushEventMonitor DONT_MONITOR_ANYTHING=(pushEvent) -> {};

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.notifyOwnerForNonMergeablePr.enabled", havingValue = "true")
    @AutoConfigureOrder(1)
    public PushEventHandler notificationHandler(RemoteSourceControl gitHub, List<Notifier> notifiers) {
        return new NotificationsHandler(gitHub, notifiers);
    }

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.closeOldPullRequests.enabled", havingValue = "true")
    @AutoConfigureOrder(2)
    public PushEventHandler pullRequestCleaningHandler(RemoteSourceControl gitHub,
                                                       @Value("${cidroid-behavior.closeOldPullRequests.limitInDays}") int prAgeLimitInDays) {
        return new PullRequestCleaningHandler(gitHub, LocalDateTime::now, prAgeLimitInDays);
    }

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.tryToRebaseOpenPrs.enabled", havingValue = "true")
    @AutoConfigureOrder(3)
    public PushEventHandler rebaseHandler(RemoteSourceControl gitHub, @Value("${source-control.login}") String gitLogin,
                                          @Value("${source-control.password}") String gitPassword) {

        return new RebaseHandler(new GitRebaser(gitLogin, gitPassword, new GitWrapper()), gitHub);
    }

    @Bean
    @ConditionalOnMissingBean(PushEventHandler.class)
    @AutoConfigureOrder(500)
    public PushEventHandler dummyPushEventOnDefaultBranchHandler() {

        return new DummyPushEventHandler();
    }

    @Bean
    @ConditionalOnMissingBean(PushEventMonitor.class)
    @AutoConfigureOrder(500)
    public PushEventMonitor dummyPushEventMonitor() {

        return DONT_MONITOR_ANYTHING;
    }

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.bestPracticeNotifier.enabled", havingValue = "true")
    @AutoConfigureOrder(1)
    public PullRequestEventHandler bestPracticeNotifierHandler(CiDroidBehavior ciDroidBehavior, List<Notifier> notifiers,
                                                               RemoteSourceControl remoteSourceControl) {

        return new BestPracticeNotifierHandler(ciDroidBehavior.getPatternToResourceMapping(), notifiers, remoteSourceControl,
                new RestTemplateResourceFetcher());

    }

    @Bean
    @ConditionalOnProperty(value = "cidroid-behavior.maxFilesInPRNotifier.enabled", havingValue = "true")
    @AutoConfigureOrder(2)
    public PullRequestEventHandler pullRequestSizeCheckHandler(CiDroidBehavior ciDroidBehavior,
                                                               List<Notifier> notifiers, RemoteSourceControl remoteSourceControl,
                                                               @Value("${cidroid-behavior.maxFilesInPRNotifier.maxFiles}") int maxFiles,
                                                               @Value("${cidroid-behavior.maxFilesInPRNotifier.warningMessage}") String warningMessage) {

        return new PullRequestSizeCheckHandler(notifiers, remoteSourceControl, maxFiles, warningMessage);

    }

    @Bean
    @ConditionalOnMissingBean(PullRequestEventHandler.class)
    @AutoConfigureOrder(500)
    public PullRequestEventHandler dummyPullRequestEventHandler() {

        return new DummyPullRequestEventHandler();
    }

    @Bean
    @ConditionalOnProperty(prefix = "notifiers", value = "github.prComment.enable", havingValue = "true")
    public Notifier gitHubCommentOnPRnotifier(RemoteSourceControl gitHub) {

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
