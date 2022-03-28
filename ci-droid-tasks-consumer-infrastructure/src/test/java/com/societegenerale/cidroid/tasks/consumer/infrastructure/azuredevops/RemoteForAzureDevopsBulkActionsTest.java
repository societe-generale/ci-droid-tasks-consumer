package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class RemoteForAzureDevopsBulkActionsTest {

  private final static int AZUREDEVOPS_SERVER_PORT_FOR_TESTS=9901;

  private final WireMockServer wm = new WireMockServer(
      options()
          .port(AZUREDEVOPS_SERVER_PORT_FOR_TESTS)
          .usingFilesUnderDirectory("src/test/resources/azureDevops"));

  private RemoteForAzureDevopsBulkActions remote=new RemoteForAzureDevopsBulkActions("http://localhost:9901","someApiKey","platform#platform-projects");

  @BeforeEach
  void setup(){
    wm.start();
    configureFor(AZUREDEVOPS_SERVER_PORT_FOR_TESTS);
  }


  @Test
  void shouldFindRepository(){


    stubFor(WireMock.get(urlPathEqualTo("/platform/platform-projects/_apis/git/repositories/helm-chart"))
        .willReturn(aResponse()
            .withBodyFile("repositoryDetails.json")
            .withStatus(200)));

    var repo=remote.fetchRepository("helm-chart");

    assertThat(repo.get()).extracting(Repository::getFullName).isEqualTo("helm-chart");

  }



}