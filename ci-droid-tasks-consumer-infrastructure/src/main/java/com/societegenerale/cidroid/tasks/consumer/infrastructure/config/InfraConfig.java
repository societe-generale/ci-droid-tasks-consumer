package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.extensions.actionToReplicate.*;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformCommand;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.FeignRemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers.EMailActionNotifier;
import com.societegenerale.cidroid.tasks.consumer.services.*;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.HttpEventMonitor;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventMonitor;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.ActionNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

import java.util.List;
import java.util.function.Consumer;

@Configuration
@EnableFeignClients(clients = { FeignRemoteGitHub.class})
@ComponentScan
public class InfraConfig {

    private static final PushEventMonitor DONT_MONITOR_ANYTHING=(pushEvent) -> {};

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
    public SourceControlEventListener pushOnMasterListener(PullRequestEventService pullRequestEventService,
                                                           PushEventService pushEventService,
                                                           SourceControlEventMapper eventMapper) {

        return new SourceControlEventListener(pullRequestEventService,pushEventService,eventMapper);
    }

    @Bean(name = "push-on-default-branch")
    public Consumer<String> msgConsumerPush(SourceControlEventListener actionToPerformListener) {

        return  event -> {actionToPerformListener.onPushEventOnDefaultBranch(event);};
    }

    @Bean(name = "push-on-non-default-branch")
    public Consumer<String> msgConsumerPushNonDefaultBranch(SourceControlEventListener actionToPerformListener) {

        return  event -> {actionToPerformListener.onPushEventOnNonDefaultBranch(event);};
    }

    @Bean(name = "pull-request-event")
    public Consumer<String> msgConsumerPREvent(SourceControlEventListener actionToPerformListener) {

        return  event -> {actionToPerformListener.onPullRequestEvent(event);};
    }

    @Bean
    public PushEventService pushEventService(RemoteSourceControl remoteSourceControl,
                                             List<PushEventHandler> pushEventHandlers,
                                             CiDroidBehavior ciDroidBehavior,
                                             PushEventMonitor pushEventMonitor) {

        return new PushEventService(remoteSourceControl, pushEventHandlers,ciDroidBehavior.isPushEventsMonitoringRequired(),pushEventMonitor);
    }

    @Bean
    public PushEventMonitor pushEventMonitoringHandler(CiDroidBehavior ciDroidBehavior) {

        if(ciDroidBehavior.isPushEventsMonitoringRequired()){
            return new HttpEventMonitor();
        }
        else{
            return DONT_MONITOR_ANYTHING;
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
