package com.societegenerale.cidroid.tasks.consumer.infrastructure.github.mocks;

import com.societegenerale.cidroid.tasks.consumer.services.model.Message;
import com.societegenerale.cidroid.tasks.consumer.services.model.User;
import com.societegenerale.cidroid.tasks.consumer.services.notifiers.Notifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class NotifierMock implements Notifier {

    @Getter
    private List<Pair<User,Message>> notifications=new ArrayList<>();

    @Override
    public void notify(com.societegenerale.cidroid.tasks.consumer.services.model.User user, Message message, Map<String, Object> additionalInfos) {
        notifications.add(new ImmutablePair(user, message));
    }
}
