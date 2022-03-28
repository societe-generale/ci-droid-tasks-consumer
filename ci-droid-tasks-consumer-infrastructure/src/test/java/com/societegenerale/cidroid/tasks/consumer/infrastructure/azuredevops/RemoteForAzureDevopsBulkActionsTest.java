package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class RemoteForAzureDevopsBulkActionsTest {

  private final static int AZUREDEVOPS_SERVER_PORT_FOR_TESTS=9902;

  private final WireMockServer wm = new WireMockServer(
      options()
          .port(AZUREDEVOPS_SERVER_PORT_FOR_TESTS)
          .usingFilesUnderDirectory("src/test/resources/azureDevops"));

  private final RemoteForAzureDevopsBulkActions remote=new RemoteForAzureDevopsBulkActions("http://localhost:"+AZUREDEVOPS_SERVER_PORT_FOR_TESTS,
      "someApiKey","platform#platform-projects");

  @BeforeEach
  void setup(){
    wm.start();
    configureFor(AZUREDEVOPS_SERVER_PORT_FOR_TESTS);
  }


  @AfterEach
  void cleanup(){
    wm.stop();
  }


  @Test
  void shouldFindRepository(){


    stubFor(WireMock.get(urlPathEqualTo("/platform/platform-projects/_apis/git/repositories/helm-chart"))
        .willReturn(aResponse()
            .withBodyFile("repositoryDetails.json")
            .withStatus(200)));

    var repo=remote.fetchRepository("helm-chart");

    assertThat(repo.get().getFullName()).isEqualTo("helm-chart");

  }



}