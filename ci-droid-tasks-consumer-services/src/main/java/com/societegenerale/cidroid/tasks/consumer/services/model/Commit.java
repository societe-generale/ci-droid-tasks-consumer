package com.societegenerale.cidroid.tasks.consumer.services.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import lombok.Builder;
import lombok.Data;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;

@Data
@Builder
public class Commit {

    private String id;

    private String url;

    private User author;

    private List<String> addedFiles;

    private List<String> modifiedFiles;

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

    @Nonnull
    public Set<String> getImpactedFiles() {
        return Stream.of(getAddedFiles(),getRemovedFiles(),getModifiedFiles()).flatMap(List::stream).collect(toSet());
    }
}

