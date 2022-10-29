package ru.mephi.test.console_platformer.expetions;

public class InvalidRoute extends RuntimeException{

    public InvalidRoute(String errorMessage) {
        super(String.format("It is impossible to build a route: %s",errorMessage));
    }

    public InvalidRoute(String errorMessage,Throwable throwable) {
        super(String.format("Cancel route construction: %s",errorMessage),throwable);
    }
}
