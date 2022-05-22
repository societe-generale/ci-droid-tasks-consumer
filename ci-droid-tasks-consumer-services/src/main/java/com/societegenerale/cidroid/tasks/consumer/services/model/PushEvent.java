package com.societegenerale.cidroid.tasks.consumer.services.model;

import java.util.List;
import javax.annotation.Nonnull;


public interface PushEvent extends SourceControlEvent {

    String getRef();

    void setRef(String ref);

    String getUserEmail();

    String getUserName();

    Commit getHeadCommit();

    int getNbCommits();

    @Nonnull
    List<Commit> getCommits();

    default boolean happenedOnDefaultBranch(){
        return getRef().endsWith(getRepository().getDefaultBranch());
    }

    void setRawEvent(String rawEvent);

    /**
     * even if not used directly here, may be useful for people leveraging on CI-droid to implement custom behavior
     * @return
     */
    String getRawEvent();
}
