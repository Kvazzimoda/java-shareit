package ru.practicum.shareit.exception;

public class WrongDateValidationException extends RuntimeException {
    public WrongDateValidationException(String message) {
        super(message);
    }
}
