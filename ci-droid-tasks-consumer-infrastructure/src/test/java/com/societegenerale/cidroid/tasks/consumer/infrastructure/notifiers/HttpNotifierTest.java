package com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.TargetHttpBackendForNotifier;
import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks.TargetHttpBackendForNotifier.TARGET_HTTP_BACKEND_MOCK_PORT;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpNotifierTest {

    private HttpNotifier httpNotifier;

    private TargetHttpBackendForNotifier httpBackendServer;

    @BeforeEach
    public void setUp(){
        String notifierUrl = "http://localhost:" + TARGET_HTTP_BACKEND_MOCK_PORT + "/notify";
        httpNotifier = new HttpNotifier(notifierUrl);

        httpBackendServer = new TargetHttpBackendForNotifier();
        httpBackendServer.start();
    }

    @AfterEach
    public void tearDown() {
        httpBackendServer.stop();
    }

    @Test
    public void shouldSendData() {
        String emailUser = "toto@socgen.com";
        User user = User.builder().email(emailUser).build();

        String messageContent = "message content";
        Message message = new Message(messageContent);

        httpNotifier.notify(user, message, emptyMap());

        assertThat(httpBackendServer.getNotificationsReceived()).hasSize(1);

        String notification = httpBackendServer.getNotificationsReceived().get(0);

        assertThat(notification).contains(emailUser, messageContent);
    }

}
