package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.GitHubMockServer;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.NotifierMock;
import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { InfraConfig.class, TestConfig.class },
        initializers = YamlFileApplicationContextInitializer.class)
public class PullRequestNotificationsIT {

    private static final String OWNER_LOGIN = "octocat";
    private static final String OWNER_EMAIL = "octocat@github.com";
    private static final int PULL_REQUEST_ID = 1347;

    @Autowired
    private GithubEventListener githubEventListener;

    @Autowired
    private NotifierMock notifier;

    @Autowired
    private GitHubMockServer githubMockServer;

    private PushEvent pushEvent;

    @Before
    public void setUp() throws IOException {
        githubMockServer.start();

        notifier.getNotifications().clear();

        pushEvent = new ObjectMapper().readValue(
                getClass().getClassLoader().getResourceAsStream("pushEvent.json"),
                PushEvent.class);
    }

    @After
    public void tearDown() {
        githubMockServer.stop();
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