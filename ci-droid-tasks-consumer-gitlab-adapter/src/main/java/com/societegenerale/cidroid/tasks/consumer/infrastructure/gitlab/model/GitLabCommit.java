package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.societegenerale.cidroid.tasks.consumer.services.model.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import java.util.List;
import javax.annotation.Nonnull;
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

    @Nonnull
    public List<String> getAddedFiles() {

        if(added==null){
            return emptyList();
        }

        return added;
    }

    @Nonnull
    public List<String> getModifiedFiles() {

        if(modified==null){
            return emptyList();
        }

        return modified;
    }

    @Nonnull
    public List<String> getRemovedFiles() {

        if(removed==null){
            return emptyList();
        }

        return removed;
    }

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
