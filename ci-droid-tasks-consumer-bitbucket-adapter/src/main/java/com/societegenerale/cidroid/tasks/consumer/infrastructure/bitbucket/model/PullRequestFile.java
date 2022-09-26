package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestFile {

    private String contentId;

    private Path path;

    private String type;

    public com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestFile toStandardPullRequestFile(){

        return com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestFile
                .builder()
                .sha(this.contentId)
                .filename(this.path.getName())
                .build();
    }

}
