package com.societegenerale.cidroid.tasks.consumer.infrastructure.bitbucket.handler;

import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformCommand;
import org.junit.jupiter.api.Test;
import org.mockserver.verify.VerificationTimes;

import java.io.IOException;

import static org.mockserver.model.HttpRequest.request;

public class BulkActionToPerformIT extends ActionToPerformEventHandlerIT {

    @Test
    public void shouldReplaceResource() throws IOException {

        ActionToPerformCommand
                simpleReplaceResourceAction = (ActionToPerformCommand)getObjectFromJson("incomingSimpleReplaceActionCommand.json",ActionToPerformCommand.class);

        actionToPerformListener.onActionToPerform(simpleReplaceResourceAction);

        bitBucketMockClient.verify(
                request()
                        .withMethod("POST")
                        .withPath("/api/projects/public-project/repos/public-repo/pull-requests"),
                VerificationTimes.exactly(1)
        );
    }

}
