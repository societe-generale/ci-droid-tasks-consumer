package com.societegenerale.cidroid.tasks.consumer.services.model;

public interface SourceControlEvent {

    Repository getRepository();

    void setRawEvent(String rawEvent);
}
