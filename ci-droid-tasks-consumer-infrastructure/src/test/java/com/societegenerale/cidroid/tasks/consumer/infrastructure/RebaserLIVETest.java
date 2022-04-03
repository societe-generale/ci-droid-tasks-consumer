package com.societegenerale.cidroid.tasks.consumer.infrastructure;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societegenerale.cidroid.tasks.consumer.services.GitRebaser;
import com.societegenerale.cidroid.tasks.consumer.services.GitWrapper;
import com.societegenerale.cidroid.tasks.consumer.services.Rebaser;
import com.societegenerale.cidroid.tasks.consumer.services.model.PullRequest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("to launch manually and test in local on 'real' pullRequest documents")
public class RebaserLIVETest {

    Rebaser rebaser = new GitRebaser("userOne", "yourPassword", new GitWrapper());

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void test1() throws IOException {

        String prAsString = IOUtils
                .toString(RebaserLIVETest.class.getClassLoader().getResourceAsStream("mergeablePullRequest.json"), "UTF-8");

        PullRequest pr = objectMapper.readValue(prAsString, PullRequest.class);

        rebaser.rebase(pr);

    }

}
