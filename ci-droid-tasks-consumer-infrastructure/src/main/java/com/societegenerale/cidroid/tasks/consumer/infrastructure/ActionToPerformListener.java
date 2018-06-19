package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.societegenerale.cidroid.tasks.consumer.extensions.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformService;
import com.societegenerale.cidroid.tasks.consumer.services.model.BulkActionToPerform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Slf4j
public class ActionToPerformListener {

    private ActionToPerformService actionToPerformService;

    private List<ActionToReplicate> actionsToReplicate;

    private Map<String, Class<? extends ActionToReplicate>> registeredActionsToReplicate;

    public ActionToPerformListener(ActionToPerformService actionToPerformService, List<ActionToReplicate> actionsToReplicate) {
        this.actionToPerformService = actionToPerformService;
        this.actionsToReplicate = actionsToReplicate;
    }

    @PostConstruct
    protected void registerActionsToReplicate() throws DuplicatedRegisteredTypeException {

        Map<String, Long> countByType = actionsToReplicate.stream()
                .collect(Collectors.groupingBy(ActionToReplicate::getType, Collectors.counting()));

        List<Map.Entry<String, Long>> actionTypeDeclaredMoreThanOnce = countByType.entrySet().stream().filter(e -> e.getValue() > 1)
                .collect(Collectors.toList());

        if (!actionTypeDeclaredMoreThanOnce.isEmpty()) {
            throw new DuplicatedRegisteredTypeException("More than 1 action registered for type(s) : " +
                    actionTypeDeclaredMoreThanOnce.stream().map(e -> e.getKey()).collect(joining(",")));
        }

        registeredActionsToReplicate = actionsToReplicate.stream().collect(Collectors.toMap(ActionToReplicate::getType, ActionToReplicate::getClass));

    }


    @StreamListener("actions-to-perform")
    public void onActionToPerform(ActionToPerformCommand actionToPerformCommand) {

        log.info("received an action to perform {}", actionToPerformCommand);

        try {

            Map<String, String> updateActionInfos = (Map) actionToPerformCommand.getUpdateAction();

            String actionToReplicateType = updateActionInfos.get("@type").trim();

            Class<? extends ActionToReplicate> actionToReplicateToInstantiate = registeredActionsToReplicate.get(actionToReplicateType);

            if (actionToReplicateToInstantiate == null) {
                throw new UnknownActionTypeException(
                        "unknown action type " + actionToReplicateType + ": please check it has been registered correctly");
            }

            ActionToReplicate actionToReplicate = actionToReplicateToInstantiate.newInstance();
            actionToReplicate.init(updateActionInfos);

            BulkActionToPerform actionToPerform = BulkActionToPerform.builder()
                    .gitLogin(actionToPerformCommand.getGitLogin())
                    .gitPassword(actionToPerformCommand.getGitPassword())
                    .email(actionToPerformCommand.getEmail())
                    .commitMessage(actionToPerformCommand.getCommitMessage())
                    .gitHubInteraction(actionToPerformCommand.getGitHubInteractionType())
                    .resourcesToUpdate(actionToPerformCommand.getResourcesToUpdate())
                    .actionToReplicate(actionToReplicate)
                    .build();

            actionToPerformService.perform(actionToPerform);

        } catch (UnknownActionTypeException e) {
            log.error("can't map the received command to a known action type", e);
        } catch (Exception e) {
            log.warn("some unexpected error happened", e);
        }

    }

}
