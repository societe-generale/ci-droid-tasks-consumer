package com.societegenerale.cidroid.tasks.consumer.services.notifiers;

import com.societegenerale.cidroid.tasks.consumer.services.model.User;

public interface ActionNotifier {

    void notify(User recipientUser, String subject, String content);

}
