package com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.AzureDevopsCommit;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.AzureDevopsRepository;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.ContentUpdate;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.FileChange;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.FileContent;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.Item;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.NewContent;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.OpenPullRequests;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.PullRequestCreated;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.PullRequestCreation;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.PushedCommit;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.Ref;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.RefCreation;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.Refs;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.SuccessfulPush;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.azuredevops.model.UpdateRef;
import com.societegenerale.cidroid.tasks.consumer.services.SourceControlBulkActionsPerformer;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.BranchAlreadyExistsException;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.Commit;
import com.societegenerale.cidroid.tasks.consumer.services.model.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequestToCreate;
import com.societegenerale.cidroid.tasks.consumer.services.model.Reference;
import com.societegenerale.cidroid.tasks.consumer.services.model.Repository;
import com.societegenerale.cidroid.tasks.consumer.services.model.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import org.springframework.http.HttpStatus;


@Slf4j
public class RemoteForAzureDevopsBulkActions implements SourceControlBulkActionsPerformer {

  public static final String APPLICATION_JSON = "application/json";
  public static final String REFS_HEADS = "refs/heads/";
  private final OkHttpClient httpClient;

  private static final String AZURE_DEVOPS_URL = "https://dev.azure.com/";

  private static final String AZURE_DEVOPS_API_VERSION = "api-version=6.0";

  private final String azureDevopsUrl;

  private final String azureOrg;
  private final String azureProject;

  private final Request.Builder readOnlyRequestTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public RemoteForAzureDevopsBulkActions(String azureDevopsUrl, String apiKeyForReadOnlyAccess, String orgNameToSplit) {
    this.httpClient = new OkHttpClient();

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
        .header("Content-Type", APPLICATION_JSON)
        .header("Authorization", basicReadOnlyAuthentCredentials);

  }


  @Override
  public ResourceContent fetchContent(String repoFullName, String fileToFetch, String branchName) {

    String fileContentUrl = azureDevopsUrl + azureOrg + "/" + azureProject + "/_apis/git/repositories/" + repoFullName + "/items?" +
        "path=" + fileToFetch +
        "&versionDescriptor.versionType=branch" +
        "&versionDescriptor.version=" + extractBranchNameFromRef(branchName) +
        "&includeContent=true" +
        "&" + AZURE_DEVOPS_API_VERSION +
        "&$format=json";

    Request request = readOnlyRequestTemplate.url(fileContentUrl).build();

    try {

      var fileResponse = httpClient.newCall(request).execute();

      if (fileResponse.isSuccessful()) {

        var fileResponseBodyAsString=fileResponse.body().string();

        var fileContent = objectMapper.readValue(fileResponseBodyAsString, FileContent.class);

        return ResourceContent
                .builder()
                .base64EncodedContent(Base64.getEncoder().encodeToString(fileContent.getContent().getBytes(StandardCharsets.UTF_8)))
                .sha(fileContent.getCommitId())
                .build();
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

    String newContent=new String(Base64.getDecoder().decode(directCommit.getBase64EncodedContent()));

    FileChange change= FileChange.builder()
        .item(new Item(path))
        .newContent(new NewContent(newContent,"rawText"))
        .build();

    AzureDevopsCommit commit= AzureDevopsCommit.builder()
        .comment(directCommit.getCommitMessage())
        .changes(List.of(change))
        .build();

    ContentUpdate contentUpdate= ContentUpdate.builder()
        .refUpdates(List.of(new UpdateRef(REFS_HEADS +directCommit.getBranch(),directCommit.getPreviousVersionSha1())))
        .commits(List.of(commit))
        .build();

    String updateContentUrl=azureDevopsUrl+azureOrg+"/"+azureProject+"/_apis/git/repositories/"+repoFullName+"/pushes?"+AZURE_DEVOPS_API_VERSION;

    try {
      RequestBody contentUpdateBody = RequestBody.create(
          MediaType.parse(APPLICATION_JSON), objectMapper.writeValueAsString(contentUpdate));

      Request request = buildWriteRequestTemplateWith(sourceControlAccessToken)
          .url(updateContentUrl)
          .post(contentUpdateBody)
          .build();

      var updateContentResponse = httpClient.newCall(request).execute();

      if(updateContentResponse.code() == HttpStatus.UNAUTHORIZED.value()){
        throw new RemoteSourceControlAuthorizationException("could not update "+path+" on "+repoFullName+" in branch "+directCommit.getBranch());
      }

      log.info("pushed a commit for "+path+" on "+repoFullName+" in branch "+directCommit.getBranch());

      var successfulPush= objectMapper.readValue(updateContentResponse.body().string(), SuccessfulPush.class);
      PushedCommit commitInPush=successfulPush.getCommits().get(0);

      Commit postPushCommit= Commit.builder()
                .id(commitInPush.getCommitId())
                .url(commitInPush.getUrl())
                .build();

      return UpdatedResource.builder()
          .commit(postPushCommit)
          .content(new UpdatedResource.Content())
          .updateStatus(UpdatedResource.UpdateStatus.UPDATE_OK)
          .build();


    } catch (IOException e) {
     log.warn("problem while pushing an update for "+path+" on "+repoFullName+" in branch "+directCommit.getBranch(),e);
    }

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

    var pullRequestCreation= PullRequestCreation.builder()
        .sourceRefName(REFS_HEADS + newPr.getHead())
        .targetRefName(REFS_HEADS + newPr.getBase())
        .title(newPr.getTitle())
        .build();

    String createPrUrl=azureDevopsUrl+azureOrg+"/"+azureProject+"/_apis/git/repositories/"+repoFullName+"/pullrequests?"+AZURE_DEVOPS_API_VERSION;

    try {

      RequestBody prCreationBody = RequestBody.create(
          MediaType.parse(APPLICATION_JSON), objectMapper.writeValueAsString(pullRequestCreation));

      Request request = buildWriteRequestTemplateWith(sourceControlAccessToken)
          .url(createPrUrl)
          .post(prCreationBody)
          .build();

      var prCreationResponse = httpClient.newCall(request).execute();

      if(prCreationResponse.code()==HttpStatus.UNAUTHORIZED.value()){
        throw new RemoteSourceControlAuthorizationException("user permission issue to create a PR on "+repoFullName);
      }

      var pullRequestCreated=objectMapper.readValue(prCreationResponse.body().string(), PullRequestCreated.class);

      var pullRequestToReturn=new PullRequest(pullRequestCreated.getPullRequestId(),pullRequestCreated.getSourceRefName());
      pullRequestToReturn.setHtmlUrl(pullRequestCreated.getUrl());
      return pullRequestToReturn;

    } catch (IOException e) {
      log.error("problem while creating the pullRequeston repo "+repoFullName,e);
    }

    return null;
  }

  @Override
  public Reference fetchHeadReferenceFrom(String repoFullName, String branchName) {

    String escapedRefName="heads%2F"+branchName;

    String getBranchRefUrl=azureDevopsUrl+azureOrg+"/"+azureProject+"/_apis/git/repositories/"+repoFullName+"/refs?filter="+escapedRefName+"&"+AZURE_DEVOPS_API_VERSION;

    Request request = readOnlyRequestTemplate.url(getBranchRefUrl).build();

    try {
      var refResponse = httpClient.newCall(request).execute();

      var refs= objectMapper.readValue(refResponse.body().string(), Refs.class);

      if(refs.getValue().isEmpty()){
        log.warn("no reference found on branch "+branchName+" for repo "+repoFullName+" - branch doesn't exist ?");
        return null;
      }
      else{

        if(refs.getValue().size()>=2){
          log.warn("more than one reference found.. taking the first one");
        }

        Ref ref=refs.getValue().get(0);

        Reference.ObjectReference objectReference=new Reference.ObjectReference("",ref.getObjectId());

        return new Reference(ref.getName(),objectReference);
      }

    } catch (IOException e) {
      log.error("problem while fetching the head reference of "+branchName+" on "+repoFullName,e);
    }


    return null;
  }

  @Override
  public Reference createBranch(String repoFullName, String branchName, String fromReferenceSha1, String sourceControlAccessToken)
      throws BranchAlreadyExistsException, RemoteSourceControlAuthorizationException {

    String createBranchUrl=azureDevopsUrl+azureOrg+"/"+azureProject+"/_apis/git/repositories/"+repoFullName+"/refs?"+AZURE_DEVOPS_API_VERSION;

    var newRef= RefCreation.builder()
        .name(REFS_HEADS +branchName)
        .oldObjectId("0000000000000000000000000000000000000000")
        .newObjectId(fromReferenceSha1)
        .build();

    try {

      RequestBody newRefBody = RequestBody.create(
          MediaType.parse(APPLICATION_JSON), objectMapper.writeValueAsString(List.of(newRef)));

      Request request = buildWriteRequestTemplateWith(sourceControlAccessToken)
          .url(createBranchUrl)
          .post(newRefBody)
          .build();

      var branchCreationResponse = httpClient.newCall(request).execute();

      // we get a 203 when there's a user right issue, so really need to check for 200
      if(branchCreationResponse.code()!=200){

        String errorMessage="problem while creating branch "+branchName+" on repo "+repoFullName+". status code : "+branchCreationResponse.code();

        log.warn(errorMessage);

        if(branchCreationResponse.code() == HttpStatus.UNAUTHORIZED.value()){
          throw new RemoteSourceControlAuthorizationException(errorMessage);
        }

        return null;
      }

      return new Reference(newRef.getName(),new Reference.ObjectReference("",fromReferenceSha1));

    } catch (IOException e) {
      log.warn("problem while creating branch "+branchName+" on repo "+repoFullName,e);
    }

    return null;
  }

  private Builder buildWriteRequestTemplateWith(String sourceControlAccessToken) {

    String basicRWAuthentCredentials = Credentials.basic("", sourceControlAccessToken);

    return new Request.Builder()
        .header("Content-Type", APPLICATION_JSON)
        .header("Authorization", basicRWAuthentCredentials);
  }

  @Override
  public Optional<Repository> fetchRepository(String repoFullName) {

    String repositoryUrl=azureDevopsUrl+azureOrg+"/"+azureProject+"/_apis/git/repositories/"+repoFullName+"?"+AZURE_DEVOPS_API_VERSION;

    Request request = readOnlyRequestTemplate.url(repositoryUrl).build();

    try {
      var repositoryResponse = httpClient.newCall(request).execute();

      if(repositoryResponse.isSuccessful()) {

        var repo= objectMapper.readValue(repositoryResponse.body().string(), AzureDevopsRepository.class);

        return Optional.of(Repository.builder()
            .url(repo.getUrl())
            .name(repo.getName())
            .fullName(repo.getName())
            .defaultBranch((repo.getDefaultBranch()))
            .build()
        );
      }
      else{
        log.warn("no repository found for : "+repoFullName);
      }

    } catch (IOException e) {
      log.error("problem while fetching repo details for "+repoFullName,e);
    }

    return Optional.empty();
  }

  @Nonnull
  @Override
  public List<PullRequest> fetchOpenPullRequests(String repoFullName) {

    String openPRsUrl =
        azureDevopsUrl + azureOrg + "/" + azureProject + "/_apis/git/repositories/" + repoFullName + "/pullrequests?searchCriteria.status=active&"
            + AZURE_DEVOPS_API_VERSION;

    Request request = readOnlyRequestTemplate.url(openPRsUrl).build();

    try {
      var openPrsResponse = httpClient.newCall(request).execute();

      if (openPrsResponse.isSuccessful()) {

        var openPullRequests = objectMapper.readValue(openPrsResponse.body().string(), OpenPullRequests.class);

        return openPullRequests.getValue().stream().map(azPr -> new PullRequest(azPr.getPullRequestId(),azPr.getSourceRefName())).collect(Collectors.toList());

      }
      else{
        log.warn("could not get list of open PRs for "+repoFullName+". status code :"+openPrsResponse.code());
      }

    } catch (IOException e) {
        log.error("problem while fetching open PRs for "+repoFullName,e);
    }
    return emptyList();
  }

    @Override
  public User fetchCurrentUser(String sourceControlAccessToken, String emailAddress) {

    //AzureDevops doesn't allow to retrieve a user details from the personal token
    //it needs to go through Oauth2 :
    // https://docs.microsoft.com/en-us/azure/devops/organizations/accounts/manage-personal-access-tokens-via-api?view=azure-devops#authenticate-with-azure-active-directory-azure-ad-tokens

    return User.builder().email(emailAddress).login(extractUsernameFrom(emailAddress)).build();
  }

  private String extractUsernameFrom(String emailAddress) {
    return emailAddress.substring(0, emailAddress.lastIndexOf("@"));
  }
}
