package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus.UPDATE_KO_NO_REASON;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatedResource {

    private Content content;

    private Commit commit;

    @JsonIgnore
    @Builder.Default
    private com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.UpdateStatus updateStatus = UPDATE_KO_NO_REASON;


    public com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource toStandardUpdatedResource() {

        var standardContent = new com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.Content();
        standardContent.setHtmlUrl(this.content.htmlUrl);
        standardContent.setName(this.content.name);
        standardContent.setPath(this.content.path);

        var standardCommit = com.societegenerale.cidroid.tasks.consumer.services.model.Commit.builder()
                .id(this.commit.getId())
                .addedFiles(this.commit.getAddedFiles())
                .modifiedFiles(this.commit.getModifiedFiles())
                .removedFiles(this.commit.getRemovedFiles())
                .url(this.commit.getUrl())
                .author(this.commit.getAuthor().toStandardUser())
                .build();

        return com.societegenerale.cidroid.tasks.consumer.services.model.UpdatedResource.builder()
                .updateStatus(this.updateStatus)
                .content(standardContent)
                .commit(standardCommit)

                .build();

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Content {

        private String name;

        private String path;

        @JsonProperty("html_url")
        private String htmlUrl;

    }

}


