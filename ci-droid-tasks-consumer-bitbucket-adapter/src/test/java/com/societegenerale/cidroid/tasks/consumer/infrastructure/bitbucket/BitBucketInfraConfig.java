package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket;

import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.extensions.actionToReplicate.*;
import com.societegenerale.cidroid.tasks.consumer.services.*;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PullRequestEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventMonitor;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.ActionNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ComponentScan
@Slf4j
public class BitBucketInfraConfig {


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
                                                           List<ActionToReplicate> actionsToReplicate,
                                                           ActionNotifier actionNotifier) {

        return new ActionToPerformListener(actionToPerformService, actionsToReplicate,actionNotifier);
    }

@Bean
    public SourceControlEventListener pushOnMasterListener(PullRequestEventService pullRequestEventService,
            PushEventService pushEventService,
            SourceControlEventMapper eventMapper) {

        return new SourceControlEventListener(pullRequestEventService,pushEventService,eventMapper);
    }



    @Bean
    public PushEventService pushEventService(SourceControlEventsReactionPerformer remoteSourceControl,
                                             List<PushEventHandler> pushEventHandlers,
                                             PushEventMonitor pushEventMonitor) {

        return new PushEventService(remoteSourceControl, pushEventHandlers, true,pushEventMonitor);
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
    public ActionToPerformService actionToPerformService(SourceControlBulkActionsPerformer remoteSourceControl,
                                                         ActionNotificationService notificationService) {

        return new ActionToPerformService(remoteSourceControl, notificationService);
    }

    @Bean
    public ActionNotificationService actionNotificationService(ActionNotifier actionNotifier) {

        return new ActionNotificationService(actionNotifier);
    }

    @Bean
    public ActionNotifier logNotifier() {

        return new ActionNotifier() {
            @Override
            public void notify(User recipientUser, String subject, String content) {
                log.info("NOTIF for "+recipientUser.getLogin()+" : ["+subject+"] "+content);
            }
        };
    }


}
