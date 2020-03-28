package com.societegenerale.cidroid.tasks.consumer.services.model;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;
import lombok.Data;

@Data
public abstract class SourceControlEvent {

    protected Repository repository;

    protected String rawMessage;

}
