package com.societegenerale.cidroid.tasks.consumer.services;

import org.eclipse.jgit.api.errors.GitAPIException;

public class CiDroidGitApiException extends GitAPIException {

    public CiDroidGitApiException(String message) {
        super(message);
    }

}
