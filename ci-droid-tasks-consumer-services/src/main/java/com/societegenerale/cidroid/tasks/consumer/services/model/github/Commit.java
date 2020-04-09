package com.societegenerale.cidroid.tasks.consumer.services.model.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Collections.emptyList;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Commit {


    private String sha;

    private String url;

    private User author;

    @JsonProperty("added")
    private List<String> addedFiles;

    @JsonProperty("modified")
    private List<String> modifiedFiles;

    @JsonProperty("removed")
    private List<String> removedFiles;


    @Nonnull
    public List<String> getAddedFiles() {

        if(addedFiles==null){
            return emptyList();
        }

        return addedFiles;
    }

    @Nonnull
    public List<String> getModifiedFiles() {

        if(modifiedFiles==null){
            return emptyList();
        }

        return modifiedFiles;
    }

    @Nonnull
    public List<String> getRemovedFiles() {

        if(removedFiles==null){
            return emptyList();
        }

        return removedFiles;
    }



}

