package com.yellowpepper.challenge.financial.exception;

public class OperationException extends Exception {

    public OperationException(String message) {
        super(message);
    }

    public OperationException(String message, Exception e) {
        super(message, e);
    }
}
