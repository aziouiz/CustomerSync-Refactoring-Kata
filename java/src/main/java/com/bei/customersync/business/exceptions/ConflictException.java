package com.bei.customersync.business.exceptions;

public class ConflictException extends RuntimeException {
    public ConflictException(String s) {
        super(s);
    }
}
