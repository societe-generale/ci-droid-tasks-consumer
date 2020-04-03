package com.societegenerale.cidroid.tasks.consumer.services.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class PullRequestEvent implements SourceControlEvent {

    public abstract String getAction();

    public abstract void setAction(String actions);

    public abstract int getPrNumber();

    @JsonIgnore
    private String rawEvent;

    @Override
    public String getRawEvent() {
        return rawEvent;
    }

    @Override
    public void setRawEvent(String rawEvent) {
        this.rawEvent = rawEvent;
    }
}
