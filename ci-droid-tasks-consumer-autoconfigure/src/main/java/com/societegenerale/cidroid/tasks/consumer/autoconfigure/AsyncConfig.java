package com.societegenerale.cidroid.tasks.consumer.autoconfigure;

import java.util.function.Consumer;

import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformCommand;
import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformListener;
import com.societegenerale.cidroid.tasks.consumer.services.PullRequestEventService;
import com.societegenerale.cidroid.tasks.consumer.services.PushEventService;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventListener;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventMapper;
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

        return actionToPerformListener::onPushEventOnDefaultBranch;
    }

    @Bean(name = "push-on-non-default-branch")
    public Consumer<String> msgConsumerPushNonDefaultBranch(SourceControlEventListener actionToPerformListener) {

        return actionToPerformListener::onPushEventOnNonDefaultBranch;
    }

    @Bean(name = "pull-request-event")
    public Consumer<String> msgConsumerPREvent(SourceControlEventListener actionToPerformListener) {

        return actionToPerformListener::onPullRequestEvent;
    }

    @Bean(name = "actions-to-perform")
    public Consumer<ActionToPerformCommand> msgConsumer(ActionToPerformListener actionToPerformListener) {

        return actionToPerformListener::onActionToPerform;
    }

}
