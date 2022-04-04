package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.societegenerale.cidroid.tasks.consumer.services.model.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GitLabCommit {

    private String id;

    private String url;

    private GitLabUser author;

    private List<String> added;

    private List<String> modified;

    private List<String> removed;

    public Commit toStandardCommit(){
        return Commit.builder()
                .id(id)
                .url(url)
                .author(new User(author.getName(), author.getEmail()))
                .addedFiles(added)
                .modifiedFiles(modified)
                .removedFiles(removed)
                .build();
    }

}
