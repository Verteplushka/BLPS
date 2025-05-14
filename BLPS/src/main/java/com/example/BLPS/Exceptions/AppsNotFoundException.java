package com.example.BLPS.Exceptions;

public class AppsNotFoundException extends RuntimeException{
    public AppsNotFoundException(String message){
        super(message);
    }
}
