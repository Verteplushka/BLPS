package com.example.BLPS.Exceptions;

public class NoPermissionException extends RuntimeException{
    public NoPermissionException (String message){
        super(message);
    }
}
