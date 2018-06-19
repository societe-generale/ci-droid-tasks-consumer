package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.societegenerale.cidroid.tasks.consumer.extensions.ResourceToUpdate;
import com.societegenerale.cidroid.tasks.consumer.extensions.gitHubInteractions.AbstractGitHubInteraction;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ToString(exclude = "gitPassword")
@Slf4j
public class ActionToPerformCommand {

    @NotEmpty
    private String gitLogin;

    @NotEmpty
    private String gitPassword;

    @Email
    private String email;

    @NotEmpty
    private String commitMessage;

    @NotNull
    private Object updateAction;

    @NotNull
    private AbstractGitHubInteraction gitHubInteractionType;

    @NotEmpty
    private List<ResourceToUpdate> resourcesToUpdate;

}
