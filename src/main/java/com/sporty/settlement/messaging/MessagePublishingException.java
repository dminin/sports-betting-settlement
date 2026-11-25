package com.sporty.settlement.messaging;

public class MessagePublishingException extends RuntimeException {

    public MessagePublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
