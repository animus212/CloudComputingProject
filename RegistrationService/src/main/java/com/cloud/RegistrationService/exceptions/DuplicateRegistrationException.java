package com.cloud.RegistrationService.exceptions;

public class DuplicateRegistrationException extends RuntimeException {
    public DuplicateRegistrationException(String message) { super(message); }
}
