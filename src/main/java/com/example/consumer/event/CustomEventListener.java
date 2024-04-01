package com.example.consumer.event;

import com.example.consumer.event.model.CustomEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class CustomEventListener implements ApplicationListener<CustomEvent> {

    @Override
    public void onApplicationEvent(CustomEvent event) {
        System.out.println("Received spring custom event - " + event.getMessage());
    }
}
