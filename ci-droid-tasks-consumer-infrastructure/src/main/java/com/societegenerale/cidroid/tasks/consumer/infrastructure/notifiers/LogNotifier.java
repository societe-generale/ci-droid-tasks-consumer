package com.societegenerale.cidroid.tasks.consumer.infrastructure.notifiers;

import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.ActionNotifier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogNotifier implements ActionNotifier {

  @Override
  public void notify(User recipientUser, String subject, String content) {
    log.info("NOTIF for "+recipientUser.getLogin()+" : ["+subject+"] "+content);
  }
}
