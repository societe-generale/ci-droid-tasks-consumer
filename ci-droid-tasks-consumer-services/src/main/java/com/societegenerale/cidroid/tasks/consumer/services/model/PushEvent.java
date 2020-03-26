package com.societegenerale.cidroid.tasks.consumer.services.model;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import lombok.Data;

@Data
public abstract class PushEvent implements SourceControlEvent {

    private String ref;

    private Repository repository;

    private Commit headCommit;

    @Override
    public Repository getRepository() {
        return repository;
    }

    public boolean happenedOnDefaultBranch(){
        return ref.endsWith(getRepository().getDefaultBranch());
    }
}
