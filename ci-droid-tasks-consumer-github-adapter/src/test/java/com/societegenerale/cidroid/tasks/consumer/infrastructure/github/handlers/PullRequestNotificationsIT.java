package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.handlers;

import static com.societegenerale.cidroid.tasks.consumer.services.model.PRmergeableStatus.NOT_MERGEABLE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.mocks.NotifierMock;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import org.apache.commons.lang3.tuple.Pair;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PullRequestNotificationsIT extends SourceControlEventHandlerIT {

    private static final String OWNER_LOGIN = "octocat";
    private static final String OWNER_EMAIL = "octocat@github.com";
    private static final int PULL_REQUEST_ID = 1347;

    @Autowired
    private NotifierMock notifier;

    @BeforeEach
    public void setUpNotifier() {
        notifier.getNotifications().clear();
    }

    @Test
    void shouldNotifyPullRequestOwnerIfNotMergeable() {

        githubMockServer.updatePullRequestMergeabilityStatus(NOT_MERGEABLE);

        sourceControlEventListener.onPushEventOnDefaultBranch(rawGitHubPushEvent);

        Awaitility.await().atMost(2, SECONDS)
                .until(()-> notifier.getNotifications().size()==1);

        Pair<User, Message> notification = notifier.getNotifications().get(0);

        User prOwner = notification.getKey();
        Message message = notification.getValue();

        assertThat(prOwner.getLogin()).isEqualTo(OWNER_LOGIN);
        assertThat(prOwner.getEmail()).isEqualTo(OWNER_EMAIL);

        assertThat(message.getContent())
                .startsWith("PR " + PULL_REQUEST_ID + " is not mergeable following commit");
    }

}
