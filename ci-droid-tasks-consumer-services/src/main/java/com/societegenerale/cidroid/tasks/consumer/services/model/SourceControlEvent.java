package com.societegenerale.cidroid.tasks.consumer.services.model;

import com.societegenerale.cidroid.tasks.consumer.services.model.github.Repository;


public interface SourceControlEvent {

    Repository getRepository();

    String getRawMessage();

}
