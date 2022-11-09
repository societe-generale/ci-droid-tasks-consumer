package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketPullRequestEvent implements PullRequestEvent {

    private String eventKey;

    private User actor;

    private PullRequest pullRequest;

    @Override
    public com.societegenerale.cidroid.tasks.consumer.services.model.Repository getRepository(){
        return pullRequest.toStandardPullRequest().getRepo();
    }

    @Override
    public int getPrNumber(){
        return pullRequest.getId();
    }

    @Override
    public String getAction(){
        return StringUtils.substringAfter(eventKey, "pr:");
    }

    @Override
    public void setRawEvent(String rawEvent) {

    }
}
