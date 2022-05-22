package com.societegenerale.cidroid.tasks.consumer.services;

import com.societegenerale.cidroid.tasks.consumer.services.model.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Builder;

@Builder
public class TestPushEvent implements PushEvent {

    private final Repository repository;

    private String ref;

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public void setRef(String ref) {
        this.ref=ref;
    }

    @Override
    public String getUserEmail() {
        return null;
    }

    @Override
    public String getUserName() {
        return null;
    }

    @Override
    public Commit getHeadCommit() {

        return Commit.builder()
                .url("someUrl")
                .build();
    }

    @Override
    public int getNbCommits() {
        return 0;
    }

    @Nonnull
    @Override
    public List<Commit> getCommits() {
        return null;
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

    @Override
    public void setRawEvent(String rawEvent) {

    }
    @Override
    public String getRawEvent() {
        return null;
    }
}
