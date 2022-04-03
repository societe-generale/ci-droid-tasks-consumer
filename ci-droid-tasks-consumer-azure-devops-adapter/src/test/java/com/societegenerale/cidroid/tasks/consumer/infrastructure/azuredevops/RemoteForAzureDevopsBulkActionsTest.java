package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class RemoteForAzureDevopsBulkActionsTest {

  private final static int AZUREDEVOPS_SERVER_PORT_FOR_TESTS=9902;

  private final WireMockServer wm = new WireMockServer(
      WireMockConfiguration.options()
          .port(AZUREDEVOPS_SERVER_PORT_FOR_TESTS));

  private final RemoteForAzureDevopsBulkActions remote=new RemoteForAzureDevopsBulkActions("http://localhost:"+AZUREDEVOPS_SERVER_PORT_FOR_TESTS+"/",
      "someApiKey","platform#platform-projects");

  @BeforeEach
  void setup(){
    wm.start();
    WireMock.configureFor(AZUREDEVOPS_SERVER_PORT_FOR_TESTS);
  }

  @AfterEach
  void cleanup(){
    wm.stop();
  }

  @Test
  void shouldFindRepository(){

    WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/platform/platform-projects/_apis/git/repositories/helm-chart"))
        .willReturn(WireMock.aResponse()
            .withBodyFile("repositoryDetails.json")
            .withStatus(200)));

    var repo=remote.fetchRepository("helm-chart");

    assertThat(repo.get().getFullName()).isEqualTo("helm-chart");

  }

  @Test
  void shouldFindRefs(){

    WireMock.stubFor(WireMock.get(
            WireMock.urlPathEqualTo("/platform/platform-projects/_apis/git/repositories/helm-chart/refs"))
              .withQueryParam("filter", WireMock.equalTo("heads/main"))
            .willReturn(WireMock.aResponse()
                    .withBodyFile("refs.json")
                    .withStatus(200)));

    var reference=remote.fetchHeadReferenceFrom("helm-chart","main");

    assertThat(reference.getObject().getSha()).isEqualTo("123ad1337042bff3f5f4170608498cff1c6e1090");
    assertThat(reference.getRef()).isEqualTo("refs/heads/main");
  }

  @Test
  void shouldGetOpenPrs(){

    WireMock.stubFor(WireMock.get(
                    WireMock.urlPathEqualTo("/platform/platform-projects/_apis/git/repositories/helm-chart/pullrequests"))
            .withQueryParam("searchCriteria.status", WireMock.equalTo("active"))
            .willReturn(WireMock.aResponse()
                    .withBodyFile("openPRs.json")
                    .withStatus(200)));

    var openPrs=remote.fetchOpenPullRequests("helm-chart");

    assertThat(openPrs).hasSize(1);
    assertThat(openPrs.get(0).getNumber()).isEqualTo(6468);
    assertThat(openPrs.get(0).getBranchName()).isEqualTo("refs/heads/technical/fix-503");

  }

  @Test
  void shouldGetFileContent(){

    WireMock.stubFor(WireMock.get(
                    WireMock.urlPathEqualTo("/platform/platform-projects/_apis/git/repositories/helm-chart/items"))
            .withQueryParam("path", WireMock.equalTo("pom.xml"))
            .withQueryParam("versionDescriptor.versionType", WireMock.equalTo("branch"))
            .withQueryParam("versionDescriptor.version", WireMock.equalTo("main"))
            .withQueryParam("$format", WireMock.equalTo("json"))
            .willReturn(WireMock.aResponse()
                    .withBodyFile("fileMetadata.json")
                    .withStatus(200)));

    WireMock.stubFor(WireMock.get(
                    WireMock.urlPathEqualTo("/platform/111114c0-ff82-4e2c-8b71-fbca48f259eb/_apis/git/repositories/444dcb4d-dbf5-457b-9edc-7fa86bf22561/items"))
            .withQueryParam("path", WireMock.equalTo("/pom.xml"))
            .withQueryParam("versionType", WireMock.equalTo("Branch"))
            .withQueryParam("version", WireMock.equalTo("main"))
            .withQueryParam("versionOptions", WireMock.equalTo("None"))
            .willReturn(WireMock.aResponse()
                    .withBodyFile("samplePom.xml")
                    .withStatus(200)));

    var resourceContent=remote.fetchContent("helm-chart","pom.xml","main");

    assertThat(resourceContent).isNotNull();
    assertThat(resourceContent.getSha()).isEqualTo("66333e270bd1f037f727511cb60fc7feb04a96e9");

    var decodedContent= new String(Base64.getDecoder().decode(resourceContent.getBase64EncodedContent()));
    assertThat(decodedContent).contains("<project xmlns=\"http://maven.apache.org/POM/4.0.0");
  }

  @Test
  void shouldUpdateFile() throws RemoteSourceControlAuthorizationException {

    WireMock.stubFor(WireMock.post(
                    WireMock.urlPathEqualTo("/platform/platform-projects/_apis/git/repositories/helm-chart/pushes"))
            .willReturn(WireMock.aResponse()
                    .withBodyFile("contentUpdatedSuccessfully.json")
                    .withStatus(201)));

    var someBase64content=Base64.getEncoder().encodeToString("someContent".getBytes(StandardCharsets.UTF_8));
    var commitToPush=new DirectCommit();
    commitToPush.setBase64EncodedContent(someBase64content);

    var updatedResource=remote.updateContent("helm-chart","pom.xml",commitToPush,"someToken");

    assertThat(updatedResource.getUpdateStatus()).isEqualTo(UpdatedResource.UpdateStatus.UPDATE_OK);
    }


}
