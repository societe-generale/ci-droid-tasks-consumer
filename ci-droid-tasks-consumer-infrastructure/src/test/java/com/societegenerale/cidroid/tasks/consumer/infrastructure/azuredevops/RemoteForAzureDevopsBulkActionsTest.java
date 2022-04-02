package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;


class RemoteForAzureDevopsBulkActionsTest {

  private final static int AZUREDEVOPS_SERVER_PORT_FOR_TESTS=9902;

  private final WireMockServer wm = new WireMockServer(
      options()
          .port(AZUREDEVOPS_SERVER_PORT_FOR_TESTS)
          .usingFilesUnderDirectory("src/test/resources/azureDevops"));

  private final RemoteForAzureDevopsBulkActions remote=new RemoteForAzureDevopsBulkActions("http://localhost:"+AZUREDEVOPS_SERVER_PORT_FOR_TESTS+"/",
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

  @Test
  void shouldFindRefs(){

    stubFor(WireMock.get(
            urlPathEqualTo("/platform/platform-projects/_apis/git/repositories/helm-chart/refs"))
              .withQueryParam("filter",equalTo("heads/main"))
            .willReturn(aResponse()
                    .withBodyFile("refs.json")
                    .withStatus(200)));

    var reference=remote.fetchHeadReferenceFrom("helm-chart","main");

    assertThat(reference.getObject().getSha()).isEqualTo("123ad1337042bff3f5f4170608498cff1c6e1090");
    assertThat(reference.getRef()).isEqualTo("refs/heads/main");
  }

  @Test
  void shouldGetOpenPrs(){

    stubFor(WireMock.get(
                    urlPathEqualTo("/platform/platform-projects/_apis/git/repositories/helm-chart/pullrequests"))
            .withQueryParam("searchCriteria.status",equalTo("active"))
            .willReturn(aResponse()
                    .withBodyFile("openPRs.json")
                    .withStatus(200)));

    var openPrs=remote.fetchOpenPullRequests("helm-chart");

    assertThat(openPrs).hasSize(1);
    assertThat(openPrs.get(0).getNumber()).isEqualTo(6468);
    assertThat(openPrs.get(0).getBranchName()).isEqualTo("refs/heads/technical/fix-503");

  }

}
