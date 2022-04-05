package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String login;

    private String email;

    public com.societegenerale.cidroid.tasks.consumer.services.model.User toStandardUser() {

        return new com.societegenerale.cidroid.tasks.consumer.services.model.User(login,email);
    }
}
