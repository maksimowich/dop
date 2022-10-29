package ru.mephi.test.console_platformer.expetions;


public class IllegalPointException extends RuntimeException{
    public IllegalPointException(String errorMessage) {
        super(String.format("It is impossible to set a point because: %s",errorMessage));
    }

    public IllegalPointException(String errorMessage,Throwable throwable) {
        super(String.format("Reject of set point: %s",errorMessage),throwable);
    }
}
