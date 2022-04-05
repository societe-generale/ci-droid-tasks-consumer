package com.societegenerale.cidroid.tasks.consumer.services;

import java.util.List;

import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import org.apache.commons.lang3.tuple.Pair;

public interface Rebaser {

    Pair<PullRequest,List<GitCommit>> rebase(PullRequest pr) ;
}
