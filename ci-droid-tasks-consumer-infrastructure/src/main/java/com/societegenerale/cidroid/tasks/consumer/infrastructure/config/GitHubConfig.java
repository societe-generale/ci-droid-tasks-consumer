package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.GithubEventListener;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.GitHubPushEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.function.Consumer;

@Configuration
@Profile("!gitLab")
public class GitHubConfig {

    @Bean(name = "push-on-default-branch")
    public Consumer<GitHubPushEvent> msgConsumerPush(GithubEventListener actionToPerformListener) {

        return  action -> {actionToPerformListener.onGitHubPushEventOnDefaultBranch(action);};
    }


    @Bean(name = "pull-request-event")
    public Consumer<PullRequestEvent> msgConsumerPREvent(GithubEventListener actionToPerformListener) {

        return  action -> {actionToPerformListener.onGitHubPullRequestEvent(action);};
    }
}
