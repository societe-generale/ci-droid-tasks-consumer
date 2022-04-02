package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource;
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

  @Test
  void shouldGetFileContent(){

    stubFor(WireMock.get(
                    urlPathEqualTo("/platform/platform-projects/_apis/git/repositories/helm-chart/items"))
            .withQueryParam("path",equalTo("pom.xml"))
            .withQueryParam("versionDescriptor.versionType",equalTo("branch"))
            .withQueryParam("versionDescriptor.version",equalTo("main"))
            .withQueryParam("$format",equalTo("json"))
            .willReturn(aResponse()
                    .withBodyFile("fileMetadata.json")
                    .withStatus(200)));

    stubFor(WireMock.get(
                    urlPathEqualTo("/platform/111114c0-ff82-4e2c-8b71-fbca48f259eb/_apis/git/repositories/444dcb4d-dbf5-457b-9edc-7fa86bf22561/items"))
            .withQueryParam("path",equalTo("/pom.xml"))
            .withQueryParam("versionType",equalTo("Branch"))
            .withQueryParam("version",equalTo("main"))
            .withQueryParam("versionOptions",equalTo("None"))
            .willReturn(aResponse()
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

    stubFor(WireMock.post(
                    urlPathEqualTo("/platform/platform-projects/_apis/git/repositories/helm-chart/pushes"))
            .willReturn(aResponse()
                    .withBodyFile("contentUpdatedSuccessfully.json")
                    .withStatus(201)));

    var someBase64content=Base64.getEncoder().encodeToString("someContent".getBytes(StandardCharsets.UTF_8));
    var commitToPush=new DirectCommit();
    commitToPush.setBase64EncodedContent(someBase64content);

    var updatedResource=remote.updateContent("helm-chart","pom.xml",commitToPush,"someToken");

    assertThat(updatedResource.getUpdateStatus()).isEqualTo(UpdatedResource.UpdateStatus.UPDATE_OK);
    }


}
