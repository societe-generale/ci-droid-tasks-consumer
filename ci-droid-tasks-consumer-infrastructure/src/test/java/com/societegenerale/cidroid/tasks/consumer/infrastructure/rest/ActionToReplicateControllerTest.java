package com.societegenerale.cidroid.tasks.consumer.infrastructure.rest;

import com.societegenerale.cidroid.api.gitHubInteractions.PullRequestGitHubInteraction;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.TestUtils;
import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformCommand;
import com.societegenerale.cidroid.tasks.consumer.services.ActionToPerformListener;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActionToReplicateController.class)
class ActionToReplicateControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private ActionToPerformListener mockActionToPerformListener;

  private String bulkUpdatePayload= TestUtils.readFileToString("/bulkUpdates/payload.json");

  @Test
  void shouldConvertPayloadInIndividualActions() throws Exception {

    performPOSTandExpectSuccess(bulkUpdatePayload);

    ArgumentCaptor<ActionToPerformCommand> actionCaptor=ArgumentCaptor.forClass(ActionToPerformCommand.class);
    verify(mockActionToPerformListener,times(4)).onActionToPerform(actionCaptor.capture());

    var firstCapturedCommand=actionCaptor.getAllValues().get(0);

   // at controller level, the actual action is not available yet, as it requires special processing that happens downstream
    assertThat(firstCapturedCommand.getGitHubInteractionType()).isInstanceOf(PullRequestGitHubInteraction.class);

  }



  private void performPOSTandExpectSuccess(String requestBodyContent) throws Exception {
    mvc.perform(post("/cidroid-actions/bulkUpdates")
            .contentType(APPLICATION_JSON)
            .content(requestBodyContent))
        .andExpect(status().is2xxSuccessful());
  }

}
