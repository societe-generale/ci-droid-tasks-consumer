package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_NO_REASON;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatedResource {

    private String id;

    private String diplayId;

    private User author;

    @JsonIgnore
    @Builder.Default
    private com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus updateStatus = UPDATE_KO_NO_REASON;

}


