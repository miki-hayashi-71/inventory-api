package com.example.inventory_api.service.exception;

public class CategoryNameDuplicateException extends RuntimeException{
    public CategoryNameDuplicateException(String message) {
        super(message);
    }
}
