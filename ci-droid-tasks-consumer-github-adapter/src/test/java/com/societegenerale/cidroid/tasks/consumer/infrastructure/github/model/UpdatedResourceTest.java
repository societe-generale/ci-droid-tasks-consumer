package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdatedResourceTest {

    @Test
    public void notUpdatedResourceCreation() {

        UpdatedResource updatedResource = UpdatedResource
                .notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_FILE_CONTENT_IS_SAME, "linkToResource");

        assertThat(updatedResource.hasBeenUpdated()).isFalse();
        assertThat(updatedResource.getContent().getHtmlUrl()).isEqualTo("linkToResource");

    }
}
