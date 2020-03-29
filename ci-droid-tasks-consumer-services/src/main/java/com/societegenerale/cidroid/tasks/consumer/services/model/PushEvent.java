package com.societegenerale.cidroid.tasks.consumer.services.model;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.Commit;



public abstract class PushEvent implements SourceControlEvent {

    public abstract String getRef();

    public abstract void setRef(String ref);

    public abstract String getUserEmail();

    public abstract String getUserName();

    public abstract Commit getHeadCommit();

    public boolean happenedOnDefaultBranch(){
        return getRef().endsWith(getRepository().getDefaultBranch());
    }
}
