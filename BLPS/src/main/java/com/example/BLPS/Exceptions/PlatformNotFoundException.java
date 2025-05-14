package com.example.BLPS.Exceptions;

public class PlatformNotFoundException extends RuntimeException{
    public PlatformNotFoundException(String message){
        super(message);
    }
}
