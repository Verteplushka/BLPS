package com.example.BLPS.Exceptions;

public class AppNotFoundException extends RuntimeException{
    public AppNotFoundException(String message){
        super(message);
    }
}
