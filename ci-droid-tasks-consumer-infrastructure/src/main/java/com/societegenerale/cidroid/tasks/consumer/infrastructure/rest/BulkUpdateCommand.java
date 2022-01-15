package com.societegenerale.cidroid.tasks.consumer.infrastructure.rest;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import com.societegenerale.cidroid.api.gitHubInteractions.AbstractGitHubInteraction;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformCommand;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "gitHubOauthToken")
public class BulkUpdateCommand {

    @ApiModelProperty(position = 1, value="the GitHub Oauth token you've generated, to be able to commit", example = "abcdefghijklmnop", required = true)
    @NotEmpty
    private String gitHubOauthToken;

    @ApiModelProperty(position = 2, value="your email ID, receive the notification for each resource updated", example = "firstname.lastname@gmail.com", required = true)
    @Email
    private String email;

    @ApiModelProperty(position = 3, value="the commit message we'll use for all updated resources",
                                    notes = "it will be suffixed by a CI-droid specific message, to identify clearly commits performed by CI-droid",
                                    example = "change performed in bulk",
                                    required = true)
    @NotEmpty
    private String commitMessage;


    @ApiModelProperty(position = 4, value="one of the available actions, provided in the GET /cidroid-actions/availableActions endpoint", required = true)
    @NotNull
    private ActionToReplicate updateAction;

    @ApiModelProperty(position = 5, value="do you want to commit directly or make a pull request ?", allowableValues = ".DirectPushGitHubInteraction, .PullRequestGitHubInteraction", required = true)
    @Valid
    private AbstractGitHubInteraction gitHubInteractionType;

    @ApiModelProperty(position = 6, required = true)
    @NotEmpty
    private List<ResourceToUpdate> resourcesToUpdate;

    public ActionToPerformCommand toActionForSingleResource(ResourceToUpdate resourceToUpdate) {

        ActionToPerformCommand action=new ActionToPerformCommand();
        action.setGitHubOauthToken(gitHubOauthToken);
        action.setEmail(email);
        action.setCommitMessage(commitMessage);
        /*
        BulkUpdateCommand singleResourceUpdateCommand = BulkUpdateCommand.builder()
                .gitHubOauthToken(bulkUpdateCommand.getGitHubOauthToken())
                .email(bulkUpdateCommand.getEmail())
                .gitHubInteractionType(bulkUpdateCommand.getGitHubInteractionType())
                .updateAction(bulkUpdateCommand.getUpdateAction())
                .commitMessage(bulkUpdateCommand.getCommitMessage())
                // creating SINGLE resource update command
                .resourcesToUpdate(Arrays.asList(resourceToUpdate))
                .build();
        */

        return action;
    }
}
