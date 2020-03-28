package com.societegenerale.cidroid.tasks.consumer.services.eventhandlers;


import com.societegenerale.cidroid.tasks.consumer.services.model.PushEvent;

@FunctionalInterface
public interface PushEventMonitor {

    void record(PushEvent pushEvent);
}
