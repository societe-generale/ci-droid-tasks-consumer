package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import java.util.function.Consumer;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformCommand;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventListener;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.SourceControlEventMapper;
import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "synchronous-mode", havingValue = "false", matchIfMissing = true)
public class AsyncConfig {

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

    @Bean(name = "actions-to-perform")
    public Consumer<ActionToPerformCommand> msgConsumer(ActionToPerformListener actionToPerformListener) {

        return  action -> {actionToPerformListener.onActionToPerform(action);};
    }

}
