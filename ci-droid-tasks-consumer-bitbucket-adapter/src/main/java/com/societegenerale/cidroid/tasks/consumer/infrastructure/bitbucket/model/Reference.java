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

    private String ref;

    private ObjectReference object;

    public com.societegenerale.cidroid.tasks.consumer.services.model.Reference toStandardReference() {

        com.societegenerale.cidroid.tasks.consumer.services.model.Reference.ObjectReference
                objRef= new com.societegenerale.cidroid.tasks.consumer.services.model.Reference.ObjectReference(object.type, object.sha);

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
