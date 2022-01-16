package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import java.util.List;
import java.util.Map;

import com.societegenerale.cidroid.tasks.consumer.services.GitCommit;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.RemoteSourceControl;
import com.societegenerale.cidroid.tasks.consumer.services.model.SourceControlEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
public class RebaseHandler implements PushEventHandler {

    private Rebaser rebaser;
    private RemoteSourceControl gitHub;

    public RebaseHandler(Rebaser rebaser, RemoteSourceControl gitHub) {
        this.rebaser = rebaser;
        this.gitHub=gitHub;
    }

    @Override
    public void handle(SourceControlEvent event, List<PullRequest> pullRequests) {

        log.info("handling rebase for {} PRs on repo {}", pullRequests.size(), event.getRepository().getUrl());

        Map<PullRequest, List<GitCommit>> rebasedCommits = pullRequests.stream()
                // rebase only the mergeable PRs
                .filter(PullRequest::getMergeable)
                // we probably don't have the rights to push anything on the forked repo to rebase the PR,
                // so not even trying to rebase if PR originates from a forked repo
                .filter(this::keepPullRequestOnlyIfNotMadeFromFork)
                .map(rebaser::rebase)
                .collect(toMap(Pair::getKey, Pair::getValue));

        log.info("{} PR(s) were rebased",rebasedCommits.keySet().size());

        for (Map.Entry<PullRequest, List<GitCommit>> commitsForSinglePr : rebasedCommits.entrySet()) {

            PullRequest pr = commitsForSinglePr.getKey();

            List rebasedCommitsForPr = commitsForSinglePr.getValue();

            if(isNotEmpty(pr.getWarningMessageDuringRebasing())){

                log.info("adding a warn comment on PR {}", pr.getNumber());
                gitHub.addCommentOnPR(pr.getRepo().getFullName(), pr.getNumber(), new Comment("There was a problem during the rebase/push process :\n"+pr.getWarningMessageDuringRebasing()));

                if(!rebasedCommitsForPr.isEmpty()){
                    log.warn("since PR was marked with a warn message, no rebased commits should be reported.. please check what happened - a bug ??");
                }
            }

            if (!rebasedCommitsForPr.isEmpty()) {

                String comment = buildPrComment(rebasedCommitsForPr);

                log.info("adding an INFO comment on PR {}", pr.getNumber());

                gitHub.addCommentOnPR(pr.getRepo().getFullName(), pr.getNumber(), new Comment(comment));
            }
        }

    }

    private boolean keepPullRequestOnlyIfNotMadeFromFork(PullRequest pr) {

        if(pr.isMadeFromForkedRepo()){
            log.info("PR {} on repo {} is made from a forked repo - not trying to rebase it",pr.getNumber(),pr.getRepo().getName());
            return false;
        }
        else{
            return true;
        }

    }

    private String buildPrComment(List<GitCommit> commits) {

        StringBuilder sb = new StringBuilder("CI-droid has rebased below ").append(commits.size()).append(" commit(s):\n");

        for (GitCommit commit : commits) {
            sb.append("- ").append(commit.getCommitId()).append(" / ").append(commit.getCommitMessage()).append("\n");
        }

        return sb.toString();
    }
}
