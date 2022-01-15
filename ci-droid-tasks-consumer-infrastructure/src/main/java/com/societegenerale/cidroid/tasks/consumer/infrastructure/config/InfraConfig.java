package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import java.util.List;

import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.extensions.actionToReplicate.AddXmlContentAction;
import com.societegenerale.cidroid.extensions.actionToReplicate.DeleteResourceAction;
import com.societegenerale.cidroid.extensions.actionToReplicate.OverwriteStaticFileAction;
import com.societegenerale.cidroid.extensions.actionToReplicate.RemoveMavenDependencyOrPluginAction;
import com.societegenerale.cidroid.extensions.actionToReplicate.RemoveXmlElementAction;
import com.societegenerale.cidroid.extensions.actionToReplicate.ReplaceMavenProfileAction;
import com.societegenerale.cidroid.extensions.actionToReplicate.SimpleReplaceAction;
import com.societegenerale.cidroid.extensions.actionToReplicate.TemplateBasedContentAction;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.FeignRemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers.EMailActionNotifier;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.rest.ActionToReplicateController;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.rest.SourceControlEventController;
import com.societegenerale.cidroid.tasks.consumer.services.ActionNotificationService;
import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformService;
import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import com.societegenerale.cidroid.tasks.consumer.services.RemoteSourceControl;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventMonitor;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.ActionNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

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
    @ConditionalOnProperty(name = "synchronous-mode", havingValue = "true")
    public SourceControlEventController sourceControlEventController(PullRequestEventService pullRequestEventService, PushEventService pushEventService,SourceControlEventMapper sourceControlEventMapper) {

        return new SourceControlEventController(pullRequestEventService, pushEventService,sourceControlEventMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "synchronous-mode", havingValue = "true")
    public ActionToReplicateController actionToReplicateController(ActionToPerformListener actionToPerformListener) {

        return new ActionToReplicateController(actionToPerformListener);
    }


    @Bean
    public PushEventService pushEventService(RemoteSourceControl remoteSourceControl,
                                             List<PushEventHandler> pushEventHandlers,
                                             CiDroidBehavior ciDroidBehavior,
                                             PushEventMonitor pushEventMonitor) {

        return new PushEventService(remoteSourceControl, pushEventHandlers,ciDroidBehavior.isPushEventsMonitoringRequired(),pushEventMonitor);
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


}
