package com.example.BLPS.Exceptions;

public class CreateAppFailedException extends RuntimeException{
    public CreateAppFailedException(String message){
        super(message);
    }
}
