package com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mockserver.model.HttpResponse;

import java.util.ArrayList;
import java.util.List;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
public class TargetHttpBackendForNotifier extends MockServer {

    public static final int TARGET_HTTP_BACKEND_MOCK_PORT = 9901;

    @Getter
    private List<String> notificationsReceived = new ArrayList<>();

    public TargetHttpBackendForNotifier() {
        super(TARGET_HTTP_BACKEND_MOCK_PORT);
    }

    @Override
    protected void initRoutes() {
        mockServer
                .when(request()
                        .withMethod("POST")
                        .withPath("/notify"))
                .respond(request -> saveNotification(request.getBodyAsString()));

    }

    private HttpResponse saveNotification(String notification) {
        notificationsReceived.add(notification);
        return response().withStatusCode(200);
    }

}
