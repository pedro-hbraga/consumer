package com.example.consumer.event.model;

import org.springframework.context.ApplicationEvent;

public class CustomEvent extends ApplicationEvent {
    private static final long serialVersionUID = 279545650698252833L;
    private String message;

    public CustomEvent(Object source, String message) {
        super(source);
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
