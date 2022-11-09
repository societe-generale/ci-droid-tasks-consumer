package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelfWithClone {
    @Nonnull
    private List<Clone> clone;
    @Nonnull
    private List<Self> self;

    public String getHttpCloneURL() {
        return clone.stream().filter(it -> "http".equals(it.getName())).findFirst().get().getHref();
    }
}
