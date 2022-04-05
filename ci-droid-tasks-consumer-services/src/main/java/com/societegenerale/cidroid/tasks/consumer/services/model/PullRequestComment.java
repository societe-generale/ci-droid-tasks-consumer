package com.societegenerale.cidroid.tasks.consumer.services.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PullRequestComment {

    private String comment;

    private User author;
}
