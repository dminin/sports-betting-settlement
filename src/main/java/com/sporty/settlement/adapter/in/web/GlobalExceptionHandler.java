package com.sporty.settlement.adapter.in.web;

import com.sporty.settlement.application.BetNotFoundException;
import com.sporty.settlement.application.port.out.MessagePublishingException;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps application errors to RFC 9457 problem details; framework errors (validation, unreadable body,
 * type mismatch) are handled by {@link ResponseEntityExceptionHandler}.
 */
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problem = ex.getBody();
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, this::messageOf, (first, second) -> first,
                        TreeMap::new)));
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

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

    private String messageOf(FieldError error) {
        return error.getDefaultMessage() == null ? "is invalid" : error.getDefaultMessage();
    }
}
