package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.handlers;

import java.io.IOException;

import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformCommand;
import org.junit.jupiter.api.Test;
import org.mockserver.matchers.MatchType;
import org.mockserver.verify.VerificationTimes;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.JsonBody.json;

public class BulkActionToPerformIT extends ActionToPerformEventHandlerIT {

    @Test
    public void shouldDeleteResource() throws IOException {

        ActionToPerformCommand
                deleteResourceAction=(ActionToPerformCommand)getObjectFromJson("incomingDeleteResourceActionCommand.json",ActionToPerformCommand.class);

        actionToPerformListener.onActionToPerform(deleteResourceAction);

        gitHubMockClient.verify(
                request()
                        .withMethod("DELETE")
                        .withPath("/api/v3/repos/myOrga/myRepo/contents/JenkinsFileQuality")
                        .withBody(
                                json("{\"branch\" : \"master\"," +
                                     " \"committer\" : {\"name\" : \"octocat\",\"email\" : \"someEmail@someDomain.com\"}," +
                                     " \"message\" : \"deleting JenkinsQualityFile performed on behalf of octocat by CI-droid\"," +
                                     " \"sha\" : \"someSHA\"}",
                                     MatchType.ONLY_MATCHING_FIELDS)),
                VerificationTimes.exactly(1)
        );
    }

}
