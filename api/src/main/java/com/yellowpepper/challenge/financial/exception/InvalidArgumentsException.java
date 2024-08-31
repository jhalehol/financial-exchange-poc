package com.yellowpepper.challenge.financial.exception;

public class InvalidArgumentsException extends Exception {

    public InvalidArgumentsException(String message) {
        super(message);
    }

    public InvalidArgumentsException(String message, Exception e) {
        super(message, e);
    }
}
