package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.FeignRemoteForGitHubBulkActions;
import com.societegenerale.cidroid.tasks.consumer.infrastructure.github.FeignRemoteForGitHubEvents;
import com.societegenerale.cidroid.tasks.consumer.services.GitHubContentBase64codec;
import com.societegenerale.cidroid.tasks.consumer.services.exceptions.RemoteSourceControlAuthorizationException;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.DirectCommit;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.ResourceContent;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.UpdatedResource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes={ InfraConfig.class,LiveTestConfig.class}, initializers = YamlFileApplicationContextInitializer.class)
@TestPropertySource("/application-test.yml")
@Disabled("to launch manually and test in local - you probably need to update config before running")
class FeignRemoteSourceControlLIVETest {

    @Autowired
    private FeignRemoteForGitHubEvents feignRemoteGitHub;

    @Autowired
    private FeignRemoteForGitHubBulkActions feignRemoteGitHubForBulkActions;

    @Test
    void manualTestForPublishingComment(){

        feignRemoteGitHub.addCommentOnPR("myOrga/myProject", 2, new Comment("test Vincent"));

    }

    @Test
    void manualTestForFetchingAResource() {

        ResourceContent resourceContent = feignRemoteGitHubForBulkActions.fetchContent("myOrga/myProject", "null", "master");

        assertThat(resourceContent).isNull();
    }

    @Test
    void manualTestForUpdatingAResource() throws RemoteSourceControlAuthorizationException {

        DirectCommit directCommit = new DirectCommit();
        directCommit.setBranch("master");
        directCommit.setCommitter(new DirectCommit.Committer("vincent-fuchs", "vincent.fuchs@gmail.com"));
        directCommit.setCommitMessage("test performed on behalf of vincent-fuchs by CI-droid");

        directCommit.setBase64EncodedContent(GitHubContentBase64codec.encode("hello world with OAuth token"));

        UpdatedResource updatedResource = feignRemoteGitHubForBulkActions.updateContent("myOrga/myProject",
                                                                          "testFile3.txt",
                                                                          directCommit, "someToken123456");

        assertThat(updatedResource).isNull();
    }

}