package com.sporty.settlement.application.port.out;

public class MessagePublishingException extends RuntimeException {

    public MessagePublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
