package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.util;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.model.*;
import com.societegenerale.cidroid.tasks.consumer.services.model.DirectCommit;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static java.util.List.of;

public class TestUtils {

    public static Repository repository() {
        var selfWithClone = getSelfWithClone();
        var repository = Repository.builder().defaultBranch("master").name("ci-droid-task-consumer").fullName("ci-droid-task-consumer").forkable(true)
                .links(selfWithClone).build();
        return repository;
    }

    public static DirectCommit directCommit() {
        var someBase64content = Base64.getEncoder().encodeToString("updated content".getBytes(StandardCharsets.UTF_8));
        var commitToPush=new DirectCommit();
        commitToPush.setBase64EncodedContent(someBase64content);
        commitToPush.setBranch("master");
        commitToPush.setPreviousVersionSha1("previous sha");
        commitToPush.setCommitMessage("committed by ci droid");
        return commitToPush;
    }

    public static PullRequestComment pullRequestComment() {
        var pullRequestComment=new PullRequestComment();
        pullRequestComment.setAction("COMMENTED");
        pullRequestComment.setUser(new User("sekhar", "some.mail@gmail.com"));
        pullRequestComment.setComment(new Comment("committed by ci droid"));
        pullRequestComment.setId("id");
        return pullRequestComment;
    }


    public static PullRequestFile pullRequestFile() {
        return new PullRequestFile("content_id", new Path("docker"),"sha_key");
    }

    public static com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestToCreate pullRequestToCreate() {
        return com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestToCreate.builder()
                .title("pull request for feature 1")
                .base("master")
                .head("feature")
                .build();
    }
    public static Reference reference() {
        var reference = new Reference();
        reference.setId("sha key");
        return reference;
    }

    public static RepositoryToCreatePullRequest repositoryToCreatePullRequest() {
        var selfWithClone = getSelfWithClone();
        return RepositoryToCreatePullRequest.builder()
                .id(1)
                .project(new Project("Ci-droid"))
                .forkable(true)
                .links(selfWithClone)
                .slug("ci-droid-consumer")
                .build();
    }

    public static SelfWithClone getSelfWithClone() {
        return new SelfWithClone(of(new Clone("clone url", "http")), of(new Self()));
    }

    public static User getUser() {
        return new User("sekhar", "some.mail@gmail.com");
    }


}
