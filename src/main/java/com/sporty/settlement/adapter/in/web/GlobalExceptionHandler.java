package com.sporty.settlement.adapter.in.web;

import com.sporty.settlement.application.BetNotFoundException;
import com.sporty.settlement.application.port.out.MessagePublishingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps application errors to RFC 9457 problem details; framework errors (validation, unreadable body,
 * type mismatch) are handled by {@link ResponseEntityExceptionHandler}.
 */
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BetNotFoundException.class)
    ProblemDetail handleBetNotFound(BetNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MessagePublishingException.class)
    ProblemDetail handlePublishingFailure(MessagePublishingException ex) {
        log.error("Message publishing failed", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Message broker is unavailable, please retry later");
    }
}
