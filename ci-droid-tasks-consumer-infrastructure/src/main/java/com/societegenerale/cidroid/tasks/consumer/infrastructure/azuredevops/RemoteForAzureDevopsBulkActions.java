package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequestToCreate;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.User;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.bouncycastle.util.encoders.Base64;


@Slf4j
public class RemoteForAzureDevopsBulkActions implements SourceControlBulkActionsPerformer {

  private final OkHttpClient httpClient;

  private final String AZURE_DEVOPS_URL = "https://dev.azure.com/";

  private final String AZURE_DEVOPS_API_VERSION = "api-version=7.1-preview.1";

  private final String azureDevopsUrl;

  private final String apiKeyForReadOnlyAccess;

  private final String azureOrg;
  private final String azureProject;

  private final Request.Builder readOnlyRequestTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public RemoteForAzureDevopsBulkActions(String azureDevopsUrl, String apiKeyForReadOnlyAccess, String orgNameToSplit) {
    this.httpClient = new OkHttpClient();

    this.apiKeyForReadOnlyAccess = apiKeyForReadOnlyAccess;
    this.azureDevopsUrl = Objects.requireNonNullElse(azureDevopsUrl, AZURE_DEVOPS_URL);

    if(orgNameToSplit!=null) {
      String[] splitedOrgName = orgNameToSplit.split("#");
      azureOrg = splitedOrgName[0];
      azureProject = splitedOrgName[1];
    }
    else{
      azureOrg =null;
      azureProject = null;
    }
    String basicReadOnlyAuthentCredentials = Credentials.basic("", apiKeyForReadOnlyAccess);

    readOnlyRequestTemplate = new Request.Builder()
        .header("Content-Type", "application/json")
        .header("Authorization", basicReadOnlyAuthentCredentials);

  }


  @Override
  public ResourceContent fetchContent(String repoFullName, String branchName, String fileToFetch) {

    String fileContentUrl = azureDevopsUrl + azureOrg + "/" + azureProject + "/_apis/git/repositories/" + repoFullName + "/items?" +
        "path=" + fileToFetch +
        "&versionDescriptor.versionType=branch" +
        "&versionDescriptor.version=" + extractBranchNameFromRef(branchName) +
        "&" + AZURE_DEVOPS_API_VERSION +
        "&$format=json";

    Request request = readOnlyRequestTemplate.url(fileContentUrl).build();

    try {

      var fileMetadataResponse = httpClient.newCall(request).execute();

      if (fileMetadataResponse.isSuccessful()) {

        var fileMetadata = objectMapper.readValue(fileMetadataResponse.body().string(), FileMetadata.class);

        var fileContentResponse = httpClient.newCall(readOnlyRequestTemplate.url(fileMetadata.getUrl()).build()).execute();

        var fileContent = new ResourceContent();
        fileContent.setBase64EncodedContent(Base64.encode(fileContentResponse.body().bytes()).toString());
        fileContent.setSha(fileMetadata.getCommitId());
        return fileContent;
      }
      else{
        log.info("file not found : "+fileToFetch+" on branch "+branchName);
      }

    } catch (IOException e) {
      log.error("problem while fetching the file "+fileToFetch+" on branch "+branchName,e);
    }

    return null;
  }

  private String extractBranchNameFromRef(String refName) {
    return refName.substring(refName.lastIndexOf("/") + 1);
  }

  @Override
  public UpdatedResource updateContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
      throws RemoteSourceControlAuthorizationException {
    return null;
  }

  @Override
  public UpdatedResource deleteContent(String repoFullName, String path, DirectCommit directCommit, String sourceControlAccessToken)
      throws RemoteSourceControlAuthorizationException {
    return null;
  }

  @Override
  public PullRequest createPullRequest(String repoFullName, PullRequestToCreate newPr, String sourceControlAccessToken)
      throws RemoteSourceControlAuthorizationException {
    return null;
  }

  @Override
  public Reference fetchHeadReferenceFrom(String repoFullName, String branchName) {
    return null;
  }

  @Override
  public Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String sourceControlAccessToken)
      throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {
    return null;
  }

  @Override
  public Optional<Repository> fetchRepository(String repoFullName) {
    return Optional.empty();
  }

  @Nonnull
  @Override
  public List<PullRequest> fetchOpenPullRequests(String repoFullName) {
    return null;
  }

  @Override
  public User fetchCurrentUser(String sourceControlAccessToken) {

    //AzureDevops doesn't allow to retrieve a user details from the personal token
    //it needs to go through Oauth2 :
    // https://docs.microsoft.com/en-us/azure/devops/organizations/accounts/manage-personal-access-tokens-via-api?view=azure-devops#authenticate-with-azure-active-directory-azure-ad-tokens


    return null;
  }
}
