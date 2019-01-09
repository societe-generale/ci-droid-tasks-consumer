package com.societegenerale.cidroid.tasks.consumer.services.model;

import java.time.LocalDateTime;

@FunctionalInterface
public interface DateProvider {

    LocalDateTime now();

}