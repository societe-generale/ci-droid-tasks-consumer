package com.societegenerale.cidroid.tasks.consumer.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.UUID;

import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class GitRebaserTest {

    GitWrapper mockGitWrapper=mock(GitWrapper.class);

    Git mockGit=mock(Git.class);

    Rebaser rebaser = new GitRebaser("gitLogin","gitPassword",mockGitWrapper);

    private final com.societegenerale.cidroid.tasks.consumer.services.model.Repository repo=
            com.societegenerale.cidroid.tasks.consumer.services.model.Repository.builder()
                    .url("someUrl")
                    .name("someName")
                    .cloneUrl("someCloneUrl")
                    //.fullName(FULL_REPO_NAME)
                    .defaultBranch("main")
                    .build();

    PullRequest pr =PullRequest.builder()
            .number(12365)
            .repo(repo)
            .baseBranchName("main")
            .branchName("testBestPractices")
            .mergeable(false)
            .isMadeFromForkedRepo(false)
            .build();

    File localRepoDirectory;

    Repository mockRepo = mock(Repository.class);

    Status mockStatus = mock(Status.class);

    ResetCommand mockResetCommand = mock(ResetCommand.class);

    CheckoutCommand mockCheckoutCommand = mock(CheckoutCommand.class);

    @BeforeEach
    public void setUp() throws IOException, GitAPIException {

        File tmpDirectory = createWorkingDirIfRequired();

        when(mockRepo.getRepositoryState()).thenReturn(RepositoryState.SAFE);

        when(mockGitWrapper.createRepository(anyString(),any(File.class))).thenReturn(mockGit);
        when(mockGitWrapper.openRepository(any(File.class))).thenReturn(mockGit);
        when(mockGitWrapper.cleanDirectories(mockGit)).thenReturn(emptySet());
        when(mockGitWrapper.refDoesntExist(eq(mockGit), anyString())).thenReturn(true);


        Ref mockRef = mock(Ref.class);
        ObjectId mockObjectId = mock(ObjectId.class);
        when(mockRef.getObjectId()).thenReturn(mockObjectId);

        when(mockGitWrapper.checkoutBranch(eq(mockGit), anyString(), anyBoolean())).thenReturn(mockRef);
        when(mockGitWrapper.resetRepo(mockGit)).thenReturn(mockResetCommand);

        when(mockGitWrapper.getCommitsOnWhichBranchIsLateComparedToBaseBranch(eq(mockGit), any(PullRequest.class))).thenReturn(emptyList());
        when(mockGitWrapper.getCommitsInBranchOnly(eq(mockGit), anyString(), anyString())).thenReturn(emptyList());

        when(mockGitWrapper.checkOut(mockGit)).thenReturn(mockCheckoutCommand);

        RebaseResult mockRebaseResult = mock(RebaseResult.class);
        when(mockRebaseResult.getStatus()).thenReturn(RebaseResult.Status.OK);
        when(mockGitWrapper.abortRebase(eq(mockGit), anyString())).thenReturn(mockRebaseResult);

        when(mockGit.getRepository()).thenReturn(mockRepo);

        PullResult mockPullResult = mock(PullResult.class);
        when(mockPullResult.isSuccessful()).thenReturn(true);
        when(mockGitWrapper.pull(mockGit)).thenReturn(mockPullResult);

        StatusCommand mockStatusCommand=mock(StatusCommand.class);
        when(mockGit.status()).thenReturn(mockStatusCommand);


        when(mockStatusCommand.call()).thenReturn(mockStatus);

        ListBranchCommand mockListBranchCommand=mock(ListBranchCommand.class);
        when(mockGit.branchList()).thenReturn(mockListBranchCommand);
        when(mockListBranchCommand.setListMode(ListBranchCommand.ListMode.ALL)).thenReturn(mockListBranchCommand);

        when(mockListBranchCommand.call()).thenReturn(emptyList());

        when(mockGit.checkout()).thenReturn(mockCheckoutCommand);
        when(mockCheckoutCommand.setForce(true)).thenReturn(mockCheckoutCommand);
        when(mockCheckoutCommand.setName(anyString())).thenReturn(mockCheckoutCommand);

        CreateBranchCommand mockCreateBranchCommand = mock(CreateBranchCommand.class);
        when(mockGit.branchCreate()).thenReturn(mockCreateBranchCommand);

        localRepoDirectory = new File(tmpDirectory + File.separator + pr.getRepo().getName() + File.separator);

        if (Files.exists(localRepoDirectory.toPath())) {
            FileUtils.deleteDirectory(localRepoDirectory);
        }

    }

    @AfterEach
    public void assertGitIsClosed() throws GitAPIException {

        verify(mockGit, times(1)).close();
    }

    private File createWorkingDirIfRequired() {
        String tmpDirStr = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirStr);
        if (!tmpDir.exists()) {
            boolean created = tmpDir.mkdirs();
            if (!created) {
                throw new RuntimeException("Unable to create tmp directory " + tmpDir);
            }
        }

        return tmpDir;
    }

    private void mimickExistingRepo() throws IOException {
        Files.createDirectory(localRepoDirectory.toPath());
        assertThat(Files.exists(localRepoDirectory.toPath())).isTrue();
        Path dummyFileInRepoDirectory = Files.createFile(localRepoDirectory.toPath().resolve("dummyFile.txt"));
        assertThat(Files.exists(dummyFileInRepoDirectory)).isTrue();
    }



    @Test
    public void shouldCloneRepoIfDoesntExist_thenSwitchToBranch() throws GitAPIException {

        rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).createRepository(pr.getRepo().getCloneUrl(), localRepoDirectory);

        assertSwitchToBranch();

    }

    @Test
    public void shouldPullForUpdatesIfMismatchBetweenLocalAndRemoteBranch() throws GitAPIException, IOException {

        RevCommit commitInBothBranches=buildRevCommit("testFile1.txt", "in both branches");
        RevCommit commitInRemoteBranchOnly=buildRevCommit("testFile2.txt", "in remote only");

        when(mockGitWrapper.getCommitsInBranchOnly(mockGit,"main","remotes/origin/testBestPractices")).thenReturn(asList(commitInBothBranches,commitInRemoteBranchOnly));
        when(mockGitWrapper.getCommitsInBranchOnly(mockGit,"main","testBestPractices")).thenReturn(asList(commitInBothBranches));

        when(mockGitWrapper.pull(mockGit)).thenReturn(mock(PullResult.class));

        rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).pull(mockGit);
        assertSwitchToBranch();
    }

    @Test
    public void shouldNotPullForUpdatesIfBranchContentIsSame() throws GitAPIException, IOException {

        RevCommit commit1InBothBranches=buildRevCommit("testFile1.txt", "in both branches 1");
        RevCommit commit2InRBothBranches=buildRevCommit("testFile2.txt", "in both branches 2");

        when(mockGitWrapper.getCommitsInBranchOnly(mockGit,"main","remotes/origin/testBestPractices")).thenReturn(asList(commit1InBothBranches,commit2InRBothBranches));
        when(mockGitWrapper.getCommitsInBranchOnly(mockGit,"main","testBestPractices")).thenReturn(asList(commit1InBothBranches,commit2InRBothBranches));

        rebaser.rebase(pr);

        verify(mockGitWrapper, never()).pull(mockGit);
        assertSwitchToBranch();
    }


    private void assertSwitchToBranch() throws GitAPIException {

        verify(mockGitWrapper, times(1)).createBranch(mockGit, "testBestPractices");
        verify(mockGitWrapper, times(1)).checkoutBranch(mockGit, "testBestPractices", true);
    }

    @Test
    public void shouldUpdateDefaultBranchIfRepoExists_thenSwitchToBranch() throws IOException, GitAPIException {
        mimickExistingRepo();

        rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).openRepository(localRepoDirectory);

        verify(mockGitWrapper, times(1)).checkoutBranch(mockGit, "main", false);
        verify(mockGitWrapper, times(1)).pull(mockGit);

        assertSwitchToBranch();

    }

    @Test
    public void shouldNotCreateBranchBeforeSwitchingIFitAlreadyExistsLocally() throws IOException, GitAPIException {
        mimickExistingRepo();

        when(mockGitWrapper.refDoesntExist(mockGit, "refs/heads/testBestPractices")).thenReturn(false);

        rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).openRepository(localRepoDirectory);

        verify(mockGitWrapper, times(1)).checkoutBranch(mockGit, "main", false);
        verify(mockGitWrapper, times(1)).pull(mockGit);

        verify(mockGitWrapper, never()).createBranch(mockGit, "testBestPractices");
        verify(mockGitWrapper, times(1)).checkoutBranch(mockGit, "testBestPractices", true);

    }


    @Test
    public void shouldAbortPreviousRebaseIfInitialRepoStateIsRebasing() throws GitAPIException, IOException {

        mimickExistingRepo();

        when(mockRepo.getRepositoryState()).thenReturn(RepositoryState.REBASING);

        rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).abortRebase(mockGit, "main");

    }

    @Test
    public void shouldCleanLocalRepoIfDirty_withModifiedFIle() throws GitAPIException, IOException {

        mimickExistingRepo();

        when(mockStatus.getModified()).thenReturn(new HashSet<>(asList("aModifiedFile.txt")));

        rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).cleanDirectories(mockGit);

        verify(mockResetCommand, times(1)).setRef(Constants.HEAD);
        verify(mockResetCommand, times(1)).call();

        verify(mockCheckoutCommand, times(1)).addPath("aModifiedFile.txt");
        verify(mockCheckoutCommand, times(1)).call();

    }

    @Test
    public void shouldCleanLocalRepoIfDirty_withAddedAndConflicted() throws GitAPIException, IOException {

        mimickExistingRepo();

        when(mockStatus.getConflicting()).thenReturn(new HashSet<>(asList("aConflictingFile.txt")));
        when(mockStatus.getAdded()).thenReturn(new HashSet<>(asList("anAddedFile.txt")));

        rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).cleanDirectories(mockGit);

        verify(mockResetCommand, times(1)).setRef(Constants.HEAD);
        verify(mockResetCommand, times(1)).addPath("aConflictingFile.txt");
        verify(mockResetCommand, times(1)).addPath("anAddedFile.txt");
        verify(mockResetCommand, times(1)).call();

        verify(mockCheckoutCommand, times(1)).call();

    }

    @Test
    public void shouldNotRebaseIfConflictsInBranch() throws GitAPIException, IOException {

        when(mockStatus.getConflicting()).thenReturn(new HashSet<>(asList("aConflictingFile.txt")));

        val rebaseResult = rebaser.rebase(pr);

        assertThat(rebaseResult.getLeft()).isEqualTo(pr);
        assertThat(rebaseResult.getRight()).isEmpty();

        verify(mockGitWrapper, never()).rebaseFrom(any(Git.class), anyString());

    }

    @Test
    public void shouldNotRebaseIfNoCommitsToRebase() throws GitAPIException, IOException {

        val rebaseResult = rebaser.rebase(pr);

        assertThat(rebaseResult.getLeft()).isEqualTo(pr);
        assertThat(rebaseResult.getRight()).isEmpty();

        verify(mockGitWrapper, never()).rebaseFrom(any(Git.class), anyString());

    }

    @Test
    public void shouldRebaseIfThereAreCommitsToRebase() throws GitAPIException, IOException {

        RevCommit dummyRevCommit = buildRevCommit("testFile1.txt", "dummy content");
        when(mockGitWrapper.getCommitsOnWhichBranchIsLateComparedToBaseBranch(mockGit, pr)).thenReturn(asList(dummyRevCommit));

        RebaseResult mockRebaseResult = mock(RebaseResult.class);
        when(mockGitWrapper.rebaseFrom(mockGit, "main")).thenReturn(mockRebaseResult);
        when(mockRebaseResult.getStatus()).thenReturn(RebaseResult.Status.OK);

        val rebaseResult = rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).rebaseFrom(any(Git.class), anyString());

        assertThat(rebaseResult.getLeft()).isEqualTo(pr);
        assertThat(rebaseResult.getRight()).containsOnly(new GitCommit(dummyRevCommit.getName(), dummyRevCommit.getShortMessage()));
    }

    /**
     * Impossible to mock RevCommit with Mockito (some methods are final), so no choice but build a tmp Git repo to build RevCommit instances
     */
    private RevCommit buildRevCommit(String fileName, String content) throws GitAPIException, IOException {

        String tmpFolderName="."+File.separator+"target"+File.separator+"GitRebaserTest-tmpDir-"+ UUID.randomUUID() ;

        File tmpFolder=new File(tmpFolderName);

        if(!tmpFolder.exists() && !tmpFolder.mkdirs()){
           fail("couldn't create tmp dir");
        }

        Git git = Git.init().setDirectory(tmpFolder).call();

        File file = new File(git.getRepository().getWorkTree(), fileName);
        try (FileOutputStream os = new FileOutputStream(file)) {
            os.write(content.getBytes(UTF_8));
        }

        git.add().addFilepattern(fileName).call();
        RevCommit revCommit = git.commit().setMessage("committed " + fileName).call();
        git.getRepository().close();

        return revCommit;

    }

}
