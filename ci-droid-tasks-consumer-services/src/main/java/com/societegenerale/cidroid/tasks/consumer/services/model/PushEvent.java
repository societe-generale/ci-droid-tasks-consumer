package com.societegenerale.cidroid.tasks.consumer.services.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Commit;

import javax.annotation.Nonnull;
import java.util.List;


public abstract class PushEvent implements SourceControlEvent {

    public abstract String getRef();

    public abstract void setRef(String ref);

    public abstract String getUserEmail();

    public abstract String getUserName();

    public abstract Commit getHeadCommit();

    public abstract int getNbCommits();

    @Nonnull
    public abstract List<Commit> getCommits();

    public boolean happenedOnDefaultBranch(){
        return getRef().endsWith(getRepository().getDefaultBranch());
    }

    @JsonIgnore
    private String rawEvent;

    @Override
    public String getRawEvent() {
        return rawEvent;
    }

    @Override
    public void setRawEvent(String rawEvent) {
        this.rawEvent = rawEvent;
    }
}
