package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reference {

    private String id;

    private ObjectReference displayId;

    public com.societegenerale.cidroid.tasks.consumer.services.model.Reference toStandardReference(String ref) {

        com.societegenerale.cidroid.tasks.consumer.services.model.Reference.ObjectReference
                objRef= new com.societegenerale.cidroid.tasks.consumer.services.model.Reference.ObjectReference("", id);

        return com.societegenerale.cidroid.tasks.consumer.services.model.Reference
                .builder()
                .ref(ref)
                .object(objRef)
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ObjectReference {

        private String type;

        private String sha;

    }

}
