package com.societegenerale.cidroid.tasks.consumer.services.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@Slf4j
@AllArgsConstructor
public class PullRequest {

    private final int number;

    private Boolean mergeable;

    private User user;

    private String url;

    private Repository repo;

    private String branchName;

    private String baseBranchName;

    private String branchStartedFromCommit;

    private LocalDateTime creationDate;

    private String htmlUrl;

    private boolean isMadeFromForkedRepo;

    private String warningMessageDuringRebasing;

    public PullRequest(int number) {
        this.number = number;
    }

    public PullRequest(int number, String onBranch) {
        this.number = number;
        this.branchName=onBranch;
    }

    public boolean doneOnBranch(String branch){
        return branchName.equals(branch);
    }

    public PRmergeableStatus getMergeStatus(){
        return PRmergeableStatus.mapping.get(mergeable);
    }

}
