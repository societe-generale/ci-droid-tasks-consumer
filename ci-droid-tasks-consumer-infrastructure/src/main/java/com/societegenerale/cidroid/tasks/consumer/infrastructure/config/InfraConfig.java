package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.extensions.actionToReplicate.*;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformCommand;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.FeignRemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers.EMailActionNotifier;
import com.societegenerale.cidroid.tasks.consumer.services.*;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventMonitoringHandler;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.ActionNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;

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
                                                           List<ActionToReplicate> actionsToReplicate, RemoteSourceControl remoteSourceControl, ActionNotifier actionNotifier) {

        return new ActionToPerformListener(actionToPerformService, actionsToReplicate, remoteSourceControl,actionNotifier);
    }

    @Bean
    public SourceControlEventListener pushOnMasterListener(PushEventService pushOnDefaultBranchService,
                                                           PullRequestEventService pullRequestEventService,
                                                           PushEventService pushOnNonDefaultBranchService) {

        return new SourceControlEventListener(pushOnDefaultBranchService, pullRequestEventService,pushOnNonDefaultBranchService);
    }

    @Bean
    public PushEventService pushOnDefaultBranchService(RemoteSourceControl remoteSourceControl,
                                                List<PushEventHandler> pushEventHandlers,
                                                CiDroidBehavior ciDroidBehavior) {

        return new PushEventService(remoteSourceControl, pushEventHandlers);
    }

    @Bean
    public PushEventService pushOnNonDefaultBranchService(RemoteSourceControl remoteSourceControl, CiDroidBehavior ciDroidBehavior, PushEventMonitoringHandler monitoring) {

        List<PushEventHandler> pushOnNonDefaultBranchEventHandlers;

        if(ciDroidBehavior.isMonitorPushEventOnNonDefaultBranch()){
            pushOnNonDefaultBranchEventHandlers= Arrays.asList(monitoring);
        }
        else{
            pushOnNonDefaultBranchEventHandlers= emptyList();
        }

        return new PushEventService(remoteSourceControl, pushOnNonDefaultBranchEventHandlers);
    }

    @Bean
    public PushEventMonitoringHandler pushEventMonitoringHandler(CiDroidBehavior ciDroidBehavior) {

        if(ciDroidBehavior.isPushEventsMonitoringRequired()){
            return new PushEventMonitoringHandler();
        }
        else{
            return null;
        }

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
    public ActionToPerformService actionToPerformService(RemoteSourceControl remoteSourceControl,
                                                         ActionNotificationService notificationService) {

        return new ActionToPerformService(remoteSourceControl, notificationService);
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

    @Bean(name = "actions-to-perform")
    public Consumer<ActionToPerformCommand> msgConsumer(ActionToPerformListener actionToPerformListener) {

        return  action -> {actionToPerformListener.onActionToPerform(action);};
    }

}
