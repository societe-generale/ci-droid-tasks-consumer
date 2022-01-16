package com.societegenerale.cidroid.tasks.consumer.infrastructure.rest;

import javax.validation.Valid;

import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformCommand;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformListener;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
    @ApiOperation(value = "Perform an action in bulk, ie replicate it in all the resources mentioned in the command")
    public ResponseEntity<?> onBulkUpdateRequest(@RequestBody @Valid
    @ApiParam(value = "The command describing the action to perform in bulk", required = true)
            BulkUpdateCommand bulkUpdateCommand) {

        log.info("received a bulkUpdateCommand {}", bulkUpdateCommand);

        for (ResourceToUpdate resourceToUpdate : bulkUpdateCommand.getResourcesToUpdate()) {

            ActionToPerformCommand actionToPerform=bulkUpdateCommand.toActionForSingleResource(resourceToUpdate);

            log.info("passing actionToPerform for processing {}...", actionToPerform);

            actionToPerformExecutor.onActionToPerform(actionToPerform);
        }

        return ResponseEntity.accepted().build();
    }
}
