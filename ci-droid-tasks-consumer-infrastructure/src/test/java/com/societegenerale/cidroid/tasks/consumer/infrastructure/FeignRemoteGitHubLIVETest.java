package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.Comment;
import com.societegenerale.cidroid.tasks.consumer.services.model.github.ResourceContent;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes={ InfraConfig.class,LiveTestConfig.class}, initializers = YamlFileApplicationContextInitializer.class)
@TestPropertySource("/application-test.yml")
@Ignore("to launch manually and test in local")
public class FeignRemoteGitHubLIVETest {

    @Autowired
    FeignRemoteGitHub feignRemoteGitHub;

    @Test
    public void manualTestForPublishingComment(){

        // you need to update properties with gitHub.url="https://sgithub.fr.world.socgen"

        feignRemoteGitHub.addCommentDescribingRebase("myOrga/test-vf-ci-droid", 2, new Comment("test Vincent"));

    }

    @Test

    public void manualTestForFetchingAResource() {

        // you need to update properties with gitHub.url="https://sgithub.fr.world.socgen"

        ResourceContent resourceContent = feignRemoteGitHub.fetchContent("myOrga/myProject", "null", "master");

        assertThat(resourceContent).isNull();
    }

}