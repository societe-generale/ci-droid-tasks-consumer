package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class OAuthInterceptor implements RequestInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private String oauthToken;

    public OAuthInterceptor(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {

        requestTemplate.header(AUTHORIZATION_HEADER, "token " + oauthToken);
    }
}
