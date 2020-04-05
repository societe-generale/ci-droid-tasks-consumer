package com.societegenerale.cidroid.tasks.consumer.services.model.gitlab;

public class GitLabEventSanitizerUtils {

    static GitLabEvent sanitizeProject(GitLabEvent gitLabEvent){
        gitLabEvent.getRepository().setDefaultBranch(gitLabEvent.getProject().getDefaultBranch());

        gitLabEvent.getRepository().setId(gitLabEvent.getProject().getId());

        gitLabEvent.getRepository().setFullName(gitLabEvent.getProject().getFullName());

        return gitLabEvent;
    }
}
