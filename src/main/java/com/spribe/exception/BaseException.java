package com.spribe.exception;

import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import static lombok.AccessLevel.PRIVATE;

@Getter
@FieldDefaults(level = PRIVATE, makeFinal = true)
public abstract class BaseException extends RuntimeException {

    HttpStatus status;
    Object body;

    protected BaseException(HttpStatus status, Exception ex, String message, Object body) {
        super(message, ex);
        this.status = status;
        this.body = body;
    }

    protected BaseException(HttpStatus status, String message, Object body) {
        super(message);
        this.status = status;
        this.body = body;
    }

    protected BaseException(HttpStatus status, String message) {
        this(status, message, null);
    }

    protected BaseException(HttpStatus status, Object body) {
        this(status, null, body);
    }
}