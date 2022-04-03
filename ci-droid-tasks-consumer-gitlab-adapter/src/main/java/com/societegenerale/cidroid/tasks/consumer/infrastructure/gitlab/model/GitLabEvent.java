package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;

public interface GitLabEvent {

    GitLabProject getProject();

    Repository getRepository();

    void setRepository(Repository repo);
}
