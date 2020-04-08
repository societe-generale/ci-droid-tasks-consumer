package com.societegenerale.cidroid.tasks.consumer.services.model.gitlab;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;

public class GitLabEventSanitizerUtils {

    static GitLabEvent sanitizeProject(GitLabEvent gitLabEvent){

        if(gitLabEvent.getRepository()==null){
            gitLabEvent.setRepository(new Repository());
        }

        gitLabEvent.getRepository().setDefaultBranch(gitLabEvent.getProject().getDefaultBranch());

        gitLabEvent.getRepository().setId(gitLabEvent.getProject().getId());

        gitLabEvent.getRepository().setFullName(gitLabEvent.getProject().getFullName());

        return gitLabEvent;
    }
}
