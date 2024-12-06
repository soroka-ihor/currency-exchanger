package com.spribe.exception.model;

import com.spribe.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidCurrencyCodeException extends BaseException {

    public InvalidCurrencyCodeException() {
        super(HttpStatus.ACCEPTED, "You have provided one or more invalid Currency Codes. [Required format: currencies=EUR,USD,GBP,...]");
    }

    public InvalidCurrencyCodeException(String message) {
        super(HttpStatus.ACCEPTED, message);
    }
}
