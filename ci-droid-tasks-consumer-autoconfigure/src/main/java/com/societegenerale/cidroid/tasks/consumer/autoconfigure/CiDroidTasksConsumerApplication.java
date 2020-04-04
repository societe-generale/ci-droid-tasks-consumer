package com.societegenerale.cidroid.tasks.consumer.autoconfigure;

import com.societegenerale.cidroid.tasks.consumer.infrastructure.config.AsyncConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({CiDroidTasksConsumerAutoConfiguration.class, AsyncConfig.class})
@SuppressWarnings("squid:S1118") //can't add a private constructor, otherwise app won't start
public class CiDroidTasksConsumerApplication {

    @SuppressWarnings("squid:S2095")
    public static void main(String[] args) {
        SpringApplication.run(CiDroidTasksConsumerApplication.class, args);
    }
}
