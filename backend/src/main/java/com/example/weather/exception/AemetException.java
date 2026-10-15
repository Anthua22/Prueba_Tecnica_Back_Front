package com.example.weather.exception;

import org.springframework.http.HttpStatus;

/** Error al comunicarse con AEMET, ya traducido al estado HTTP que queremos devolver. */
public class AemetException extends RuntimeException {

    private final HttpStatus status;

    public AemetException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public AemetException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
