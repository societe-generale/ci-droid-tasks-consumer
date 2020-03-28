package com.societegenerale.cidroid.tasks.consumer.services.model;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.Commit;
import lombok.Data;

@Data
public abstract class PushEvent extends SourceControlEvent {

    private String ref;

    private Commit headCommit;

    public boolean happenedOnDefaultBranch(){
        return ref.endsWith(getRepository().getDefaultBranch());
    }
}
