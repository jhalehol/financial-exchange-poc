package com.yellowpepper.challenge.financial.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConfigurationException.class)
    public ResponseEntity<?> handleConfigurationException(ConfigurationException ex) {
        return buildResponse(ex, "Something in the configuration is wrong",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidArgumentsException.class)
    public ResponseEntity<?> handleInvalidArgumentsException(InvalidArgumentsException ex) {
        return buildResponse(ex, "Invalid Arguments",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex) {
        return buildResponse(ex, "Not found",
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OperationException.class)
    public ResponseEntity<?> handleOperationException(OperationException ex) {
        return buildResponse(ex, "Unable to complete the request",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleUnauthorizedException(AuthenticationException ex) {
        return buildResponse(ex, "Unauthorized to perform the given request",
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException ex) {
        return buildResponse(ex, "Unable to complete the request",
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDataException(DataAccessException ex) {
        return buildResponse(ex, "Unable to complete the request",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleDataException(RuntimeException ex) {
        return buildResponse(ex, "Something is not working properly",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return buildResponse(ex, "Access Denied",
                HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<?> buildResponse(final Exception ex, final String message,
            final HttpStatus status) {
        log.error("Handling exception in controller advisor", ex);
        Map<String, String> body = new HashMap<>();
        body.put("timestamp", String.valueOf(Instant.now()));
        body.put("message", message);
        body.put("details", ex.getMessage());
        body.put("status", status.toString());

        return new ResponseEntity<>(body, status);
    }
}
