package com.societegenerale.cidroid.tasks.consumer.services.model;



public abstract class PullRequestEvent implements SourceControlEvent {

    public abstract String getAction();

    public abstract void setAction(String actions);

    public abstract int getPrNumber();

}
