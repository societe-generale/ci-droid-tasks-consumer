package com.societegenerale.cidroid.tasks.consumer.services.model.github;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdatedResourceTest {

    @Test
    void notUpdatedResourceCreation() {

        UpdatedResource updatedResource = UpdatedResource
                .notUpdatedResource(UpdatedResource.UpdateStatus.UPDATE_KO_FILE_CONTENT_IS_SAME, "linkToResource");

        assertThat(updatedResource.hasBeenUpdated()).isFalse();
        assertThat(updatedResource.getContent().getHtmlUrl()).isEqualTo("linkToResource");

    }
}