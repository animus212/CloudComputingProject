package com.cloud.EventService.exceptions;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String message) { super(message); }
}
