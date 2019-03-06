package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.extensions.actionToReplicate.*;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.FeignRemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.GithubEventListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers.EMailActionNotifier;
import com.societegenerale.cidroid.tasks.consumer.services.*;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventOnDefaultBranchHandler;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.ActionNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

import java.util.List;

@Configuration
@EnableFeignClients(clients = { FeignRemoteGitHub.class})
@ComponentScan
public class InfraConfig {

    @Bean
    public ActionToReplicate overwriteStaticFileAction() {

        return new OverwriteStaticFileAction();
    }

    @Bean
    public ActionToReplicate replaceMavenProfileAction() {

        return new ReplaceMavenProfileAction();
    }

    @Bean
    public ActionToReplicate simpleReplaceAction() {

        return new SimpleReplaceAction();
    }

    @Bean
    public ActionToReplicate addXmlContentAction() {

        return new AddXmlContentAction();
    }

    @Bean
    public ActionToReplicate removeXmlElementAction() {

        return new RemoveXmlElementAction();
    }

    @Bean
    public ActionToReplicate templateBasedContentAction() {

        return new TemplateBasedContentAction();
    }

    @Bean
    public ActionToReplicate removeMavenDependencyOrPluginAction() {

        return new RemoveMavenDependencyOrPluginAction();
    }

    @Bean
    public ActionToReplicate deleteResourceAction() {

        return new DeleteResourceAction();
    }


    @Bean
    public ActionToPerformListener actionToPerformListener(ActionToPerformService actionToPerformService,
            List<ActionToReplicate> actionsToReplicate, RemoteGitHub remoteGitHub,ActionNotifier actionNotifier) {

        return new ActionToPerformListener(actionToPerformService, actionsToReplicate,remoteGitHub,actionNotifier);
    }

    @Bean
    public GithubEventListener pushOnMasterListener(PushEventOnDefaultBranchService pushOnDefaultBranchService,
            PullRequestEventService pullRequestEventService) {

        return new GithubEventListener(pushOnDefaultBranchService, pullRequestEventService);
    }

    @Bean
    public PushEventOnDefaultBranchService pushOnMasterService(RemoteGitHub remoteGitHub,
            List<PushEventOnDefaultBranchHandler> pushEventOnDefaultBranchHandlers) {

        return new PushEventOnDefaultBranchService(remoteGitHub, pushEventOnDefaultBranchHandlers);
    }

    @Bean
    public PullRequestEventService pullRequestEventService(List<PullRequestEventHandler> pullRequestEventHandlers) {

        return new PullRequestEventService(pullRequestEventHandlers);
    }

    @Bean
    public HttpMessageConverters feignHttpMessageConverters() {
        return new HttpMessageConverters();
    }

    @Bean
    public ActionToPerformService actionToPerformService(RemoteGitHub remoteGitHub,
            ActionNotificationService notificationService) {

        return new ActionToPerformService(remoteGitHub, notificationService);
    }

    @Bean
    public ActionNotificationService actionNotificationService(ActionNotifier actionNotifier) {

        return new ActionNotificationService(actionNotifier);
    }

    @Bean
    public ActionNotifier emailActionNotifier(MailSender javaMailSender,
            @Value("${spring.mail.sender}") String mailSender) {

        return new EMailActionNotifier(javaMailSender, mailSender);
    }

}
