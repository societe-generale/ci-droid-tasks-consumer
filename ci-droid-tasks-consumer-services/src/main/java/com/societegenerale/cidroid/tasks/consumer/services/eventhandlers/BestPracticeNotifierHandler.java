package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import com.societegenerale.cidroid.tasks.consumer.services.RemoteGitHub;
import com.societegenerale.cidroid.tasks.consumer.services.ResourceFetcher;
import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.*;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import io.github.azagniotov.matcher.AntPathMatcher;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier.PULL_REQUEST;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class BestPracticeNotifierHandler implements PullRequestEventHandler {

    private final Map<String, String> configuredPatternToContentMapping;

    private final List<Notifier> notifiers;

    private RemoteGitHub remoteGitHub;

    private ResourceFetcher resourceFetcher;

    private int maxFilesInPr;

    private String maxFilesWarningMessage;

    public BestPracticeNotifierHandler(Map<String, String> configuredPatternToContentMapping,
                                       List<Notifier> notifiers, RemoteGitHub remoteGitHub, ResourceFetcher resourceFetcher,
                                       int maxFilesInPr, String maxFilesWarningMessage) {

        this.configuredPatternToContentMapping = configuredPatternToContentMapping;
        this.notifiers = notifiers;
        this.remoteGitHub = remoteGitHub;
        this.resourceFetcher = resourceFetcher;
        this.maxFilesInPr = maxFilesInPr;
        this.maxFilesWarningMessage = maxFilesWarningMessage;
    }

    @Override
    public void handle(PullRequestEvent event) {

        List<PullRequestFile> filesInPr = remoteGitHub.fetchPullRequestFiles(event.getRepository().getFullName(), event.getPrNumber());
        List<PullRequestComment> existingPrComments = remoteGitHub.fetchPullRequestComments(event.getRepository().getFullName(), event.getPrNumber());

        StringBuilder moreThanMaxFilesInPRComment = validateNumberOfFilesInPR(filesInPr.size(), existingPrComments);

        StringBuilder bestPracticesViolationsInFilesComments = validateBestPracticesInPullRequestFiles(event, filesInPr, existingPrComments);

        if (commetExists(moreThanMaxFilesInPRComment) || commetExists(bestPracticesViolationsInFilesComments)) {
            StringBuilder bestPracticesViolationWarnings = new StringBuilder("Reminder of best practices : \n")
                    .append(moreThanMaxFilesInPRComment).append(bestPracticesViolationsInFilesComments);
            notifyComments(event, bestPracticesViolationWarnings);
        }

    }

    private StringBuilder validateNumberOfFilesInPR(int numberOfFiles, List<PullRequestComment> existingPrComments) {
        StringBuilder comment = new StringBuilder();

        if (numberOfFiles > maxFilesInPr) {
            comment.append(createMoreThanMaxFilesInPRCommentIfAlreadyNotCommented(existingPrComments));
        }

        return comment;
    }

    private StringBuilder createMoreThanMaxFilesInPRCommentIfAlreadyNotCommented(List<PullRequestComment> existingPrComments) {
        StringBuilder comment = new StringBuilder();

        String moreThanMaxFilesInPRComment = MessageFormat.format(maxFilesWarningMessage, maxFilesInPr);

        boolean alreadyCommented = existingPrComments.stream().anyMatch(existingComment -> existingComment.getComment().equals(moreThanMaxFilesInPRComment));
        if (!alreadyCommented) {
            comment.append("\n").append(moreThanMaxFilesInPRComment).append("\n");
        }

        return comment;
    }

    private StringBuilder validateBestPracticesInPullRequestFiles(PullRequestEvent event, List<PullRequestFile> filesInPr,
                                                                  List<PullRequestComment> existingPrComments) {

        StringBuilder comments = new StringBuilder();

        Map<PullRequestFile, Map<String, String>> matchingPatternsByPullRequestFile = findConfiguredPatternsThatMatch(filesInPr);

        //TODO  refactor below nested loops
        if (!matchingPatternsByPullRequestFile.isEmpty()) {

            Map<PullRequestFile, Map<String, String>> matchingPatternsByPullRequestFileOnWhichWeHaventCommentedYet = findConfiguredPatternsOnWhichWehaventCommentedYet(
                    matchingPatternsByPullRequestFile, existingPrComments);

            if (!matchingPatternsByPullRequestFileOnWhichWeHaventCommentedYet.isEmpty()) {


                for (Map.Entry matchedPrFile : matchingPatternsByPullRequestFileOnWhichWeHaventCommentedYet.entrySet()) {

                    PullRequestFile prFile = (PullRequestFile) matchedPrFile.getKey();
                    Map<String, String> matchedBestPractices = (Map) matchedPrFile.getValue();

                    comments.append("- ").append(prFile.getFilename()).append(" : \n");

                    for (Map.Entry resourceToGetByPattern : matchedBestPractices.entrySet()) {

                        Optional<String> bestPracticeContent = resourceFetcher.fetch((String) resourceToGetByPattern.getValue());

                        if (bestPracticeContent.isPresent()) {
                            comments.append("\t -").append(bestPracticeContent.get()).append("\n");
                        } else {
                            log.warn("best practice located at {} doesn't seem to exist..", resourceToGetByPattern.getValue());
                        }
                    }
                }
            }
        }
        return comments;
    }

    private Map<PullRequestFile, Map<String, String>> findConfiguredPatternsOnWhichWehaventCommentedYet(
            Map<PullRequestFile, Map<String, String>> patternsByPullRequestFileToFilter, List<PullRequestComment> existingPrComments) {

        return patternsByPullRequestFileToFilter.entrySet()
                .stream()
                .filter(prFile -> hasntReceivedAnyCommentOnFileYet(prFile.getKey().getFilename(), existingPrComments))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

    private boolean hasntReceivedAnyCommentOnFileYet(String fileName, List<PullRequestComment> existingPrComments) {

        return existingPrComments.stream().map(prComment -> prComment.getComment())
                .noneMatch(comment -> comment.contains(fileName));

    }

    private Map findConfiguredPatternsThatMatch(List<PullRequestFile> filesInPr) {

        Map<PullRequestFile, Map<String, String>> matchingPatternsByPullRequestFile = new HashMap<>();

        AntPathMatcher pathMatcher = new AntPathMatcher.Builder().withIgnoreCase().build();

        for (PullRequestFile file : filesInPr) {

            Map<String, String> matchingPatterns = configuredPatternToContentMapping.entrySet().stream()
                    .filter(entry -> pathMatcher.isMatch(entry.getKey(), file.getFilename()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (!matchingPatterns.isEmpty()) {
                matchingPatternsByPullRequestFile.put(file, matchingPatterns);
            }

        }

        return matchingPatternsByPullRequestFile;

    }

    private void notifyComments(PullRequestEvent event, StringBuilder bestPracticesWarnings) {
        PullRequest pr = remoteGitHub.fetchPullRequestDetails(event.getRepository().getFullName(), event.getPrNumber());
        Map<String, Object> additionalInfosForNotification = new HashMap();
        additionalInfosForNotification.put(PULL_REQUEST, pr);

        notifiers.stream().forEach(n -> n.notify(new User(), new Message(bestPracticesWarnings.toString()), additionalInfosForNotification));
    }

    private boolean commetExists(StringBuilder warnings) {
        return 0 != warnings.length();
    }
}
