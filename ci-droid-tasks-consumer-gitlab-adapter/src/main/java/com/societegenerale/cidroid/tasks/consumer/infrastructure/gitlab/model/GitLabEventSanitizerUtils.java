package com.societegenerale.cidroid.tasks.consumer.infrastructure.gitlab.model;

import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;

public class GitLabEventSanitizerUtils {

    static GitLabEvent sanitizeProject(GitLabEvent gitLabEvent){

        if(gitLabEvent.getRepository()==null){
            gitLabEvent.setRepository(Repository.builder().build());
        }

        gitLabEvent.getRepository().setDefaultBranch(gitLabEvent.getProject().getDefaultBranch());

        gitLabEvent.getRepository().setId(gitLabEvent.getProject().getId());

        gitLabEvent.getRepository().setFullName(gitLabEvent.getProject().getFullName());

        return gitLabEvent;
    }
}
