package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MergeResult {
    private String outcome;
    private boolean current;
}
