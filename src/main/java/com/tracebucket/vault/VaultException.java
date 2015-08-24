package com.tracebucket.vault;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

public class VaultException extends RuntimeException implements Serializable {

    private String message;

    private HttpStatus httpStatus;

    public VaultException(String message, HttpStatus httpStatus) {
        super(message);
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}