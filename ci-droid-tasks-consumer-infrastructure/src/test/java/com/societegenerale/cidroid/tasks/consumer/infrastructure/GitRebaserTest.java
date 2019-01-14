package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneeyedmen.fakir.Faker;
import com.societegenerale.cidroid.tasks.consumer.services.GitCommit;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.PullRequest;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class GitRebaserTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    GitWrapper mockGitWrapper=mock(GitWrapper.class);

    Git mockGit=mock(Git.class);

    Rebaser rebaser = new GitRebaser("gitLogin","gitPassword",mockGitWrapper);

    PullRequest pr;

    File localRepoDirectory;

    Repository mockRepo = mock(Repository.class);

    Status mockStatus = mock(Status.class);

    ResetCommand mockResetCommand = mock(ResetCommand.class);

    CheckoutCommand mockCheckoutCommand = mock(CheckoutCommand.class);

    @Before
    public void setup() throws IOException, GitAPIException {

        File tmpDirectory = createWorkingDirIfRequired();

        String prAsString = IOUtils
                .toString(GitRebaserTest.class.getClassLoader().getResourceAsStream("mergeablePullRequest.json"), "UTF-8");

        pr = new ObjectMapper().readValue(prAsString, PullRequest.class);

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

    @After
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

        when(mockGitWrapper.getCommitsInBranchOnly(mockGit,"master","remotes/origin/testBestPractices")).thenReturn(asList(commitInBothBranches,commitInRemoteBranchOnly));
        when(mockGitWrapper.getCommitsInBranchOnly(mockGit,"master","testBestPractices")).thenReturn(asList(commitInBothBranches));

        when(mockGitWrapper.pull(mockGit)).thenReturn(Faker.fakeA(PullResult.class));

        rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).pull(mockGit);
        assertSwitchToBranch();
    }

    @Test
    public void shouldNotPullForUpdatesIfBranchContentIsSame() throws GitAPIException, IOException {

        RevCommit commit1InBothBranches=buildRevCommit("testFile1.txt", "in both branches 1");
        RevCommit commit2InRBothBranches=buildRevCommit("testFile2.txt", "in both branches 2");

        when(mockGitWrapper.getCommitsInBranchOnly(mockGit,"master","remotes/origin/testBestPractices")).thenReturn(asList(commit1InBothBranches,commit2InRBothBranches));
        when(mockGitWrapper.getCommitsInBranchOnly(mockGit,"master","testBestPractices")).thenReturn(asList(commit1InBothBranches,commit2InRBothBranches));

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

        verify(mockGitWrapper, times(1)).checkoutBranch(mockGit, "master", false);
        verify(mockGitWrapper, times(1)).pull(mockGit);

        assertSwitchToBranch();

    }

    @Test
    public void shouldNotCreateBranchBeforeSwitchingIFitAlreadyExistsLocally() throws IOException, GitAPIException {
        mimickExistingRepo();

        when(mockGitWrapper.refDoesntExist(mockGit, "refs/heads/testBestPractices")).thenReturn(false);

        rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).openRepository(localRepoDirectory);

        verify(mockGitWrapper, times(1)).checkoutBranch(mockGit, "master", false);
        verify(mockGitWrapper, times(1)).pull(mockGit);

        verify(mockGitWrapper, never()).createBranch(mockGit, "testBestPractices");
        verify(mockGitWrapper, times(1)).checkoutBranch(mockGit, "testBestPractices", true);

    }


    @Test
    public void shouldAbortPreviousRebaseIfInitialRepoStateIsRebasing() throws GitAPIException, IOException {

        mimickExistingRepo();

        when(mockRepo.getRepositoryState()).thenReturn(RepositoryState.REBASING);

        rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).abortRebase(mockGit, "master");

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
        when(mockGitWrapper.rebaseFrom(mockGit, "master")).thenReturn(mockRebaseResult);
        when(mockRebaseResult.getStatus()).thenReturn(RebaseResult.Status.OK);

        val rebaseResult = rebaser.rebase(pr);

        verify(mockGitWrapper, times(1)).rebaseFrom(any(Git.class), anyString());

        assertThat(rebaseResult.getLeft()).isEqualTo(pr);
        assertThat(rebaseResult.getRight()).containsOnly(new GitCommit(dummyRevCommit.getName(), dummyRevCommit.getShortMessage()));
    }

    /**
     * Impossible to mock RevCommit with Mockito (some methods are final), so non choice but build a tmp Git repo to build RevCommit instances
     */
    private RevCommit buildRevCommit(String fileName, String content) throws GitAPIException, IOException {

        Git git = Git.init().setDirectory(tmpFolder.getRoot()).call();

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