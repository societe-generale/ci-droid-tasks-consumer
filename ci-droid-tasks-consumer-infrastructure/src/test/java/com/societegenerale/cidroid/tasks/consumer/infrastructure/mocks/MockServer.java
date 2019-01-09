package com.societegenerale.cidroid.tasks.consumer.infrastructure.mocks;

import org.mockserver.integration.ClientAndServer;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

public abstract class MockServer {

    private int port;
    ClientAndServer mockServer;

    MockServer(int port) {
        this.port = port;
    }

    public void start() {
        if (mockServer != null && mockServer.isRunning()) {
            return;
        }
        mockServer = startClientAndServer(port);
        initRoutes();
    }

    protected abstract void initRoutes();

    public void stop() {
        if (mockServer.isRunning()) {
            mockServer.stop();
        }
    }

}