package com.societegenerale.cidroid.tasks.consumer.infrastructure.handlers;

import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.gitHubInteractions.DirectPushGitHubInteraction;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.ActionToPerformCommand;
import com.societegenerale.cidroid.tasks.consumer.services.model.DeleteResourceAction;
import org.junit.Test;
import org.mockserver.verify.VerificationTimes;

import java.io.IOException;
import java.util.Arrays;

import static org.mockserver.model.HttpRequest.request;

public class BulkActionToPerformIT extends ActionToPerformEventHandlerIT {


    @Test
    public void shouldDeleteResource() throws IOException {

        ActionToPerformCommand deleteResourceAction=(ActionToPerformCommand)getObjectFromJson("incomingDeleteResourceActionCommand.json",ActionToPerformCommand .class);

        actionToPerformListener.onActionToPerform(deleteResourceAction);

        gitHubMockClient.verify(
                request()
                        .withMethod("DELETE")
                        .withPath("/api/v3/repos/myOrg/myRepo/contents/JenkinsFileQuality")
                        .withBody("{\"message\":\"some commit message\",\"sha\":\"123456\",\"branch\":\"master\"}"),
                VerificationTimes.once()
        );
    }

}