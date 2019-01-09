package com.societegenerale.cidroid.tasks.consumer.infrastructure.handlers;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.NotifierMock;
import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class PullRequestNotificationsIT extends GitHubEventHandlerIT {

    private static final String OWNER_LOGIN = "octocat";
    private static final String OWNER_EMAIL = "octocat@github.com";
    private static final int PULL_REQUEST_ID = 1347;

    @Autowired
    private NotifierMock notifier;

    @Before
    public void setUpNotifier() {
        notifier.getNotifications().clear();
    }

    @Test
    public void shouldNotifyPullRequestOwnerIfNotMergeable() {
        githubEventListener.onGitHubPushEventOnDefaultBranch(pushEvent);

        await().atMost(2, SECONDS)
                .until(() -> assertThat(notifier.getNotifications()).hasSize(1));

        Pair<User, Message> notification = notifier.getNotifications().get(0);

        User prOwner = notification.getKey();
        Message message = notification.getValue();

        assertThat(prOwner.getLogin()).isEqualTo(OWNER_LOGIN);
        assertThat(prOwner.getEmail()).isEqualTo(OWNER_EMAIL);

        assertThat(message.getContent())
                .startsWith("PR " + PULL_REQUEST_ID + " is not mergeable following commit");
    }

}