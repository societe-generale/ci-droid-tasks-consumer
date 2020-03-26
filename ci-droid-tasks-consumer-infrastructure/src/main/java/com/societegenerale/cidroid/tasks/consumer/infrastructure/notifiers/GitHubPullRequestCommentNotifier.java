package com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers;

import com.societegenerale.cidroid.tasks.consumer.services.RemoteSourceControl;
import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class GitHubPullRequestCommentNotifier implements Notifier {

    private RemoteSourceControl remoteSourceControl;

    public GitHubPullRequestCommentNotifier(RemoteSourceControl remoteSourceControl) {
        this.remoteSourceControl = remoteSourceControl;

    }

    @Override
    public void notify(User user, Message message,Map<String,Object> additionalInfos) {

        PullRequest pr=(PullRequest)additionalInfos.get(PULL_REQUEST);

        remoteSourceControl.addCommentOnPR(pr.getRepo().getFullName(),pr.getNumber(),new Comment(message.getContent()));

    }


}
