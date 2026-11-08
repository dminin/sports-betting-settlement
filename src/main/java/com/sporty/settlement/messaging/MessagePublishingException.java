package com.sporty.settlement.messaging;

/** Indicates that a broker did not accept an outbound message. */
public class MessagePublishingException extends RuntimeException {

  /** Creates an exception with operation context and the broker failure. */
  public MessagePublishingException(String message, Throwable cause) {
    super(message, cause);
  }
}
