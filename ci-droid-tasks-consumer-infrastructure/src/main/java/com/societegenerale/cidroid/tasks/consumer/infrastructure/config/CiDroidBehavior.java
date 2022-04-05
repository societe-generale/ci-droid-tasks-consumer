package com.societegenerale.cidroid.tasks.consumer.infrastructure.config;

import java.util.Map;

import lombok.Data;


@Data
public class CiDroidBehavior {

    private Map<String, String> patternToResourceMapping;

    private boolean monitorPushEventOnDefaultBranch= false;
    private boolean monitorPushEventOnNonDefaultBranch=false;

    public boolean isPushEventsMonitoringRequired(){
        return monitorPushEventOnDefaultBranch || monitorPushEventOnNonDefaultBranch;
    }
    
}
