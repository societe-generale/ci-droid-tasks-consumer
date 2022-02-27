package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import static java.util.stream.Collectors.toMap;

import com.societegenerale.cidroid.tasks.consumer.services.ResourceFetcher;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlEventsReactionPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestComment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestFile;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import io.github.azagniotov.matcher.AntPathMatcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BestPracticeNotifierHandler implements PullRequestEventHandler {

    private final Map<String, String> configuredPatternToContentMapping;

    private final List<Notifier> notifiers;

    private final SourceControlEventsReactionPerformer remoteSourceControl;

    private final ResourceFetcher resourceFetcher;


    public BestPracticeNotifierHandler(Map<String, String> configuredPatternToContentMapping,
                                       List<Notifier> notifiers, SourceControlEventsReactionPerformer remoteSourceControl, ResourceFetcher resourceFetcher) {

        this.configuredPatternToContentMapping = configuredPatternToContentMapping;
        this.notifiers = notifiers;
        this.remoteSourceControl = remoteSourceControl;
        this.resourceFetcher = resourceFetcher;
    }

    @Override
    public void handle(PullRequestEvent event) {

        List<PullRequestFile> filesInPr = remoteSourceControl.fetchPullRequestFiles(event.getRepository().getFullName(), event.getPrNumber());
        List<PullRequestComment> existingPrComments = remoteSourceControl.fetchPullRequestComments(event.getRepository().getFullName(), event.getPrNumber());

        StringBuilder bestPracticesViolationsWarnings = validateBestPracticesInPullRequestFiles(filesInPr, existingPrComments);

        PullRequest pullRequest = remoteSourceControl.fetchPullRequestDetails(event.getRepository().getFullName(), event.getPrNumber());
        notifyWarnings(pullRequest, bestPracticesViolationsWarnings, notifiers);

    }

    private StringBuilder validateBestPracticesInPullRequestFiles(List<PullRequestFile> filesInPr, List<PullRequestComment> existingPrComments) {

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

        return existingPrComments.stream().map(PullRequestComment::getComment)
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

}
