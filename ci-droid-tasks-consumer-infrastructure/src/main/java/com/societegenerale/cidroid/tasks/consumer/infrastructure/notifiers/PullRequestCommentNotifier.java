package com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers;

import java.util.Map;

import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PullRequestCommentNotifier implements Notifier {

    private final SourceControlEventsReactionPerformer remoteSourceControl;

    public PullRequestCommentNotifier(SourceControlEventsReactionPerformer remoteSourceControl) {
        this.remoteSourceControl = remoteSourceControl;

    }

    @Override
    public void notify(User user, Message message,Map<String,Object> additionalInfos) {

        PullRequest pr=(PullRequest)additionalInfos.get(PULL_REQUEST);

        remoteSourceControl.addCommentOnPR(pr.getRepo().getFullName(),pr.getNumber(),new Comment(message.getContent()));

    }


}
