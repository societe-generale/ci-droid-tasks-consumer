package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import com.societegenerale.cidroid.tasks.consumer.services.RemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.List;

@Slf4j
public class PullRequestSizeCheckHandler implements PullRequestEventHandler {

    private final List<Notifier> notifiers;

    private RemoteGitHub remoteGitHub;

    private int maxFilesInPr;

    private String maxFilesWarningMessage;

    public PullRequestSizeCheckHandler(List<Notifier> notifiers, RemoteGitHub remoteGitHub, int maxFilesInPr, String maxFilesWarningMessage) {
        this.notifiers = notifiers;
        this.remoteGitHub = remoteGitHub;
        this.maxFilesInPr = maxFilesInPr;
        this.maxFilesWarningMessage = maxFilesWarningMessage;
    }

    @Override
    public void handle(PullRequestEvent event) {

        List<PullRequestFile> filesInPr = remoteGitHub.fetchPullRequestFiles(event.getRepository().getFullName(), event.getPrNumber());
        List<PullRequestComment> existingPrComments = remoteGitHub.fetchPullRequestComments(event.getRepository().getFullName(), event.getPrNumber());

        StringBuilder warning = validateNumberOfFilesInPR(filesInPr.size(), existingPrComments);
        PullRequest pullRequest = remoteGitHub.fetchPullRequestDetails(event.getRepository().getFullName(), event.getPrNumber());
        notifyWarnings(pullRequest, warning, notifiers);

    }

    private StringBuilder validateNumberOfFilesInPR(int numberOfFiles, List<PullRequestComment> existingPrComments) {
        StringBuilder warning = new StringBuilder();

        if (numberOfFiles > maxFilesInPr) {
            warning.append(createMoreThanMaxFilesInPRWarningIfNotCommentedAlready(existingPrComments));
        }

        return warning;
    }

    private StringBuilder createMoreThanMaxFilesInPRWarningIfNotCommentedAlready(List<PullRequestComment> existingPrComments) {
        StringBuilder warning = new StringBuilder();

        String moreThanMaxFilesInPRWarning = MessageFormat.format(maxFilesWarningMessage, maxFilesInPr);

        if (!isAlreadyCommented(existingPrComments, moreThanMaxFilesInPRWarning)) {
            warning.append(moreThanMaxFilesInPRWarning).append("\n");
        }

        return warning;
    }

    private boolean isAlreadyCommented(List<PullRequestComment> existingPrComments, String moreThanMaxFilesInPRComment) {
        return existingPrComments.stream().anyMatch(
                existingComment -> existingComment.getComment().equals(moreThanMaxFilesInPRComment));
    }
}
