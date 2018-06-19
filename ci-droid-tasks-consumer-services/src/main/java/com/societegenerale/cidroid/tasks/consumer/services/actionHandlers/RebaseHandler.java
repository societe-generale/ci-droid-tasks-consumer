package com.societegenerale.cidroid.tasks.consumer.services.actionHandlers;

import com.societegenerale.cidroid.tasks.consumer.services.GitCommit;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.RemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.services.model.GitHubEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
public class RebaseHandler implements PushEventOnDefaultBranchHandler {

    private Rebaser rebaser;
    private RemoteGitHub gitHub;

    public RebaseHandler(Rebaser rebaser,RemoteGitHub gitHub) {
        this.rebaser = rebaser;
        this.gitHub=gitHub;

    }

    @Override
    public void handle(GitHubEvent event,List<PullRequest> pullRequests) {

        Map<PullRequest, List<GitCommit>> rebasedCommits = pullRequests.stream()
                //rebase only the mergeable PRs
                .filter(pr -> pr.getMergeable())
                .map(mergeablePr -> rebaser.rebase(mergeablePr))
                .collect(toMap(pair -> pair.getKey(), pair -> pair.getValue()));

        for (Map.Entry<PullRequest, List<GitCommit>> commitsForSinglePr : rebasedCommits.entrySet()) {

            PullRequest pr = commitsForSinglePr.getKey();

            List rebasedCommitsForPr = commitsForSinglePr.getValue();

            if(isNotEmpty(pr.getWarningMessageDuringRebasing())){

                log.info("adding a warn comment on PR {}", pr.getNumber());
                gitHub.addCommentDescribingRebase(pr.getRepo().getFullName(), pr.getNumber(), new Comment("There was a problem during the rebase/push process :\n"+pr.getWarningMessageDuringRebasing()));

                if(!rebasedCommitsForPr.isEmpty()){
                    log.warn("since PR was marked with a warn message, no rebased commits should be reported.. please check what happened - a bug ??");
                }
            }

            if (!rebasedCommitsForPr.isEmpty()) {

                String comment = buildPrComment(rebasedCommitsForPr);

                log.info("adding an INFO comment on PR {}", pr.getNumber());

                gitHub.addCommentDescribingRebase(pr.getRepo().getFullName(), pr.getNumber(), new Comment(comment));
            }
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
