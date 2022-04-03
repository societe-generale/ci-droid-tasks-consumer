package com.societegenerale.cidroid.tasks.consumer.services.model;

import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommitTest {

    @Test
    void collectImpactedFiles(){

        Commit commit=Commit.builder()
                .addedFiles(Arrays.asList("file1", "file2"))
                .removedFiles(Arrays.asList("file3", "file4","file5"))
                .modifiedFiles(Arrays.asList("file6"))
                .build();

        Set<String> impactedFiles=commit.getImpactedFiles();

        assertThat(impactedFiles).containsExactlyInAnyOrder("file1","file2","file3","file4","file5","file6");
    }

    @Test
    void impactedFilesListIsEmptyByDefault(){

        Commit commit=Commit.builder().build();

        assertThat(commit.getImpactedFiles()).isEmpty();
    }
}
