package com.societegenerale.cidroid.tasks.consumer.services.model.github;

import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommitTest {

    @Test
    void collectImpactedFiles(){

        Commit commit=new Commit();
        commit.setAddedFiles(Arrays.asList("file1", "file2"));
        commit.setRemovedFiles(Arrays.asList("file3", "file4","file5"));
        commit.setModifiedFiles(Arrays.asList("file6"));

        Set<String> impactedFiles=commit.getImpactedFiles();

        assertThat(impactedFiles).containsExactlyInAnyOrder("file1","file2","file3","file4","file5","file6");
    }

    @Test
    void impactedFilesListIsEmptyByDefault(){

        Commit commit=new Commit();

        assertThat(commit.getImpactedFiles()).isEmpty();
    }
}
