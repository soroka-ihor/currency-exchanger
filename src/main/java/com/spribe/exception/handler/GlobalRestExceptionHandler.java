package com.spribe.exception.handler;

import com.spribe.exception.model.ApiError;
import com.spribe.exception.model.InvalidCurrencyCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalRestExceptionHandler {

    @ExceptionHandler(InvalidCurrencyCodeException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(InvalidCurrencyCodeException ex) {
        return new ResponseEntity<>(
                new ApiError(
                        HttpStatus.resolve(ex.getStatus().value()),
                        ex.getMessage(),
                        ex
                ), ex.getStatus()
        );
    }

}
