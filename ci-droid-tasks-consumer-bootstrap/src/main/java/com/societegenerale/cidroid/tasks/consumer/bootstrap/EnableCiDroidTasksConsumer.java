package com.societegenerale.cidroid.tasks.consumer.bootstrap;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.InfraConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({InfraConfig.class, ActionHandlersAutoConfig.class})
public @interface EnableCiDroidTasksConsumer {
}
