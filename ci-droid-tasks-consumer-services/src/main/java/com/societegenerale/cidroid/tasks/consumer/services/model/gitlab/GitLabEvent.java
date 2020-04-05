package com.societegenerale.cidroid.tasks.consumer.services.model.gitlab;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;

public interface GitLabEvent {

    GitLabProject getProject();

    Repository getRepository();
}
