package com.societegenerale.cidroid.tasks.consumer.infrastructure.github;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.mocks.NotifierMock;
import com.societegenerale.cidroid.tasks.consumer.services.GitRebaser;
import com.societegenerale.cidroid.tasks.consumer.services.GitWrapper;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.RebaseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class LiveTestConfig {

    @Bean
    public Rebaser mockRebaser() {
        return mock(Rebaser.class);
    }

    @Bean
    public NotifierMock mockNotifier() {
        return new NotifierMock();
    }

    @Bean
    public Rebaser rebaser(@Value("${gitHub.login}") String gitLogin, @Value("${gitHub.password}") String gitPassword) {
        return new GitRebaser(gitLogin, gitPassword, new GitWrapper());
    }

    @Bean
    public PushEventHandler rebaseHandler(Rebaser rebaser, SourceControlEventsReactionPerformer remoteSourceControl) {

        return new RebaseHandler(rebaser, remoteSourceControl);
    }
}
