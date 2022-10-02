package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Collections.emptyList;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Commit {

    private String id;

    private String url;

    private User author;

   com.societegenerale.cidroid.tasks.consumer.services.model.Commit toStandardCommit(){

        return com.societegenerale.cidroid.tasks.consumer.services.model.Commit.builder()
                .id(this.id)
                .url(this.url)
                .author(com.societegenerale.cidroid.tasks.consumer.services.model.User.builder()
                        .login(author.getLogin())
                        .email(author.getEmail()).build())
                .build();

   }



}

