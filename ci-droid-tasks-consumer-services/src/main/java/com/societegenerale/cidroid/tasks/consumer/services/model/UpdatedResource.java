package com.societegenerale.cidroid.tasks.consumer.services.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class UpdatedResource {

    private Content content;

    private Commit commit;

    @Builder.Default
    private UpdateStatus updateStatus = UpdateStatus.UPDATE_KO_NO_REASON;

    public static UpdatedResource notUpdatedResource(UpdateStatus updateStatus, String htmlUrl) {

        Content contentThatWasntUpdated = new Content();
        contentThatWasntUpdated.setHtmlUrl(htmlUrl);

        UpdatedResource notUpdatedResource = UpdatedResource.builder()
                .updateStatus(updateStatus)
                .content(contentThatWasntUpdated)
                .build();

        return notUpdatedResource;
    }

    public static UpdatedResource notUpdatedResource(UpdateStatus updateStatus) {

        return notUpdatedResource(updateStatus,null);
    }

    public boolean hasBeenUpdated() {
        return updateStatus.hasBeenUpdated;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Content {

        private String name;

        private String path;

        private String htmlUrl;

    }

    public enum UpdateStatus {

        UPDATE_OK(true),
        UPDATE_OK_WITH_PR_CREATED(true),
        UPDATE_OK_WITH_PR_ALREADY_EXISTING(true),
        UPDATE_OK_BUT_PR_CREATION_KO(true),

        UPDATE_KO_FILE_DOESNT_EXIST(false),
        UPDATE_KO_FILE_CONTENT_IS_SAME(false),
        UPDATE_KO_BRANCH_CREATION_ISSUE(false),
        UPDATE_KO_CANT_PROVIDE_CONTENT_ISSUE(false),
        UPDATE_KO_NO_REASON(false),
        UPDATE_KO_AUTHENTICATION_ISSUE(false),
        UPDATE_KO_UNEXPECTED_EXCEPTION_DURING_PROCESSING(false),
        UPDATE_KO_REPO_DOESNT_EXIST(false);

        private final boolean hasBeenUpdated;

        UpdateStatus(boolean hasBeenUpdated) {
            this.hasBeenUpdated = hasBeenUpdated;

        }
    }
}


