package com.example.inventory_api.service.exception;

public class CategoryLimitExceededException extends RuntimeException{
    public CategoryLimitExceededException(String message) {
        super(message);
    }
}
