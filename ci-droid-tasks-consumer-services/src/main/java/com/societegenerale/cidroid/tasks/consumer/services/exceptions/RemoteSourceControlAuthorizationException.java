package com.societegenerale.cidroid.tasks.consumer.services.exceptions;

public class RemoteSourceControlAuthorizationException extends Exception {

    public RemoteSourceControlAuthorizationException(String message) {
        super(message);
    }

    public RemoteSourceControlAuthorizationException(String message,Throwable t) {
        super(message,t);
    }
}
