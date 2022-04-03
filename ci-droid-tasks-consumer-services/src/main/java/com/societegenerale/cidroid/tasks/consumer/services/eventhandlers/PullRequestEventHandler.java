package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestEvent;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;

import static com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier.PULL_REQUEST;

public interface PullRequestEventHandler {

    void handle(PullRequestEvent event);

    default void notifyWarnings(PullRequest pullRequest, StringBuilder bestPracticesWarnings, List<Notifier> notifiers) {
        if (warningExists(bestPracticesWarnings)) {
            Map<String, Object> additionalInfosForNotification = new HashMap();
            additionalInfosForNotification.put(PULL_REQUEST, pullRequest);

            notifiers.stream().forEach(
                    n -> n.notify(User.builder().build(), new Message(bestPracticesWarnings.toString()), additionalInfosForNotification));
        }
    }

    default boolean warningExists(StringBuilder warnings) {
        return 0 != warnings.length();
    }

}
