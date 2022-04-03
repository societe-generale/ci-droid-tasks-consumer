package com.societegenerale.cidroid.tasks.consumer.services.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Reference {

    private String ref;

    private ObjectReference object;

    @Data
    @AllArgsConstructor
    public static class ObjectReference {

        private String type;

        private String sha;

    }

    public String getBranchName(){
        return ref.substring(ref.lastIndexOf("/")+1);
    }

}
