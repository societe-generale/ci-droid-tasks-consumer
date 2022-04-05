package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.live;

import com.societegenerale.cidroid.tasks.consumer.services.GitRebaser;
import com.societegenerale.cidroid.tasks.consumer.services.GitWrapper;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.PushEventHandler;
import com.societegenerale.cidroid.tasks.consumer.services.eventhandlers.RebaseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

//TODO understand why it gets loaded during automated tests (that's why it's commented out)
//@Configuration
public class LiveTestConfig {

    @Bean
    public Rebaser rebaser(@Value("${source-control.login}") String gitLogin,
                           @Value("${source-control.password}") String gitPassword) {
        return new GitRebaser(gitLogin, gitPassword, new GitWrapper());
    }

    @Bean
    public PushEventHandler rebaseHandler(Rebaser rebaser, SourceControlEventsReactionPerformer remoteSourceControl) {

        return new RebaseHandler(rebaser, remoteSourceControl);
    }
}
