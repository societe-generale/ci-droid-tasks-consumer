package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PullRequestComment {

    private String id;

    private String action;

    private String commentAction;

    private Comment comment;

    private User user;

    public com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestComment toStandardPullRequestComment() {
        String commentAsText = "COMMENTED".equals(action) ? comment.getText() : null;
        return new com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestComment(commentAsText, user.toStandardUser());

    }

}
