package com.societegenerale.cidroid.tasks.consumer.infrastructure.rest;

import javax.validation.Valid;

import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformCommand;
import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/cidroid-actions")
@Slf4j
public class ActionToReplicateController {

    private final ActionToPerformListener actionToPerformExecutor;

    public ActionToReplicateController(ActionToPerformListener actionToPerformExecutor) {
        this.actionToPerformExecutor = actionToPerformExecutor;
    }

    @PostMapping(path = "bulkUpdates")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Perform an action in bulk, ie replicate it in all the resources mentioned in the command")
    public ResponseEntity<?> onBulkUpdateRequest(@RequestBody @Valid
    @Parameter(description = "The command describing the action to perform in bulk", required = true)
            ActionToPerformCommand bulkUpdateCommand) {

        log.info("received a bulkUpdateCommand {}", bulkUpdateCommand);

        for (ResourceToUpdate resourceToUpdate : bulkUpdateCommand.getResourcesToUpdate()) {

            ActionToPerformCommand actionToPerformForSingleResource=bulkUpdateCommand.toActionForSingleResource(resourceToUpdate);

            log.info("passing actionToPerform for processing {}...", actionToPerformForSingleResource);

            actionToPerformExecutor.onActionToPerform(actionToPerformForSingleResource);
        }

        return ResponseEntity.accepted().build();
    }
}
