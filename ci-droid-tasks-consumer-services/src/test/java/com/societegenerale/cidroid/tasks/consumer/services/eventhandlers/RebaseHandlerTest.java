package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import java.util.List;

import com.societegenerale.cidroid.tasks.consumer.services.GitCommit;
import com.societegenerale.cidroid.tasks.consumer.services.PullRequestMatcher;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.TestPushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RebaseHandlerTest {

    private static final int PULL_REQUEST_NUMBER = 1347;

    private final SourceControlEventsReactionPerformer mockRemoteSourceControl = mock(SourceControlEventsReactionPerformer.class);

    private final Rebaser mockRebaser = mock(Rebaser.class);

    private final Repository repo=Repository.builder().url("someUrl").name("someName").build();

    private PullRequest singlePr= PullRequest.builder()
            .number(PULL_REQUEST_NUMBER)
            .repo(repo)
            .mergeable(false)
            .isMadeFromForkedRepo(false)
            .build();

    private PushEvent pushEvent= TestPushEvent.builder()
            .repository(repo)
            .build();

    private RebaseHandler rebaseHandler = new RebaseHandler(mockRebaser, mockRemoteSourceControl);


    @Test
    void shouldRebaseAndPostGitHubCommentForMergeablePr() {

        singlePr.setMergeable(true);

        String commitId = "123456";

        List<GitCommit> rebasedCommitsForOnePr = singletonList(new GitCommit(commitId, "a tiny commit that was done on master"));
        Pair<PullRequest, List<GitCommit>> rebaseResult = new ImmutablePair<>(singlePr, rebasedCommitsForOnePr);
        when(mockRebaser.rebase(singlePr)).thenReturn(rebaseResult);

        rebaseHandler.handle(pushEvent, singletonList(singlePr));

        verify(mockRebaser, times(1)).rebase(singlePr);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        verify(mockRemoteSourceControl, times(1)).addCommentOnPR(eq(singlePr.getRepo().getFullName()), eq(singlePr.getNumber()), commentCaptor.capture());

        String comment = commentCaptor.getValue().getBody();

        assertThat(comment).startsWith("CI-droid has rebased below ");
        assertThat(comment).contains(commitId);

    }

    @Test
    void shouldNotRebaseWhenPRisMadeFromFork() {

        singlePr.setMergeable(true);
        singlePr.setMadeFromForkedRepo(true);

        rebaseHandler.handle(pushEvent, singletonList(singlePr));

        verify(mockRebaser, never()).rebase(singlePr);

    }

    @Test
    void shouldPostGitHubWarningCommentWhenProblemWhileRebasing() {

        singlePr.setMergeable(true);

        singlePr.setWarningMessageDuringRebasing("one commit had conflicts");

        Pair<PullRequest, List<GitCommit>> rebaseResult = new ImmutablePair<>(singlePr, emptyList());

        PullRequestMatcher matchesSimplePr = new PullRequestMatcher(PULL_REQUEST_NUMBER);

        when(mockRebaser.rebase(argThat(matchesSimplePr))).thenReturn(rebaseResult);

        rebaseHandler.handle(pushEvent, singletonList(singlePr));

        verify(mockRebaser, times(1)).rebase(argThat(matchesSimplePr));

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        verify(mockRemoteSourceControl, times(1)).addCommentOnPR(eq(singlePr.getRepo().getFullName()), eq(singlePr.getNumber()), commentCaptor.capture());

        String comment = commentCaptor.getValue().getBody();

        assertThat(comment).startsWith("There was a problem during the rebase/push process :");
        assertThat(comment).endsWith("one commit had conflicts");
    }


    @Test
    void shouldNotRebase_whenPrIsNotMergeable() {

        rebaseHandler.handle(pushEvent, singletonList(singlePr));

        verify(mockRebaser, never()).rebase(any(PullRequest.class));

    }
}
