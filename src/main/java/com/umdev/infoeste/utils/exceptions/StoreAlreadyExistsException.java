package com.umdev.infoeste.utils.exceptions;

public class StoreAlreadyExistsException extends RuntimeException {
    public StoreAlreadyExistsException(String message) {
        super(message);
    }
}