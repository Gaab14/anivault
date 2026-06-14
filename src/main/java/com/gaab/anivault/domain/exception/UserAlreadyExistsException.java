package com.gaab.anivault.domain.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String field, String value) {
        super("A user with " + field + " '" + value + "' already exists.");
    }
}
