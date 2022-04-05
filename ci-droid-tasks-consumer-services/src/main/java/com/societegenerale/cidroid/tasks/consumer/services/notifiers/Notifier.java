package com.societegenerale.cidroid.tasks.consumer.services.notifiers;

import java.util.Map;

import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;

public interface Notifier {

    String PULL_REQUEST = "pullRequest";

    //TODO put user in the additionalInfos Map
    void notify(User user, Message message, Map<String,Object> additionalInfos);

}
