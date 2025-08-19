package com.example.inventory_api.controller.advice;

import com.example.inventory_api.controller.dto.ErrorResponse;
import com.example.inventory_api.service.exception.CategoryLimitExceededException;
import com.example.inventory_api.service.exception.CategoryNameDuplicateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {

    // 400 Bad Request: 入力値のチェック
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        // 最初に見つかったエラーを取得
        FieldError fieldError = e.getBindingResult().getFieldError();

        if (fieldError == null) {
            ErrorResponse errorResponse = new ErrorResponse("VALIDATION_ERROR", "不正なリクエストです");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        String errorMessage = fieldError.getDefaultMessage();
        String errorCode;

        // エラーメッセージの内容に応じて、API仕様書のエラーコードを判定
        if (errorMessage != null) {
            if (errorMessage.contains("必須です")) {
                errorCode = "CATEGORY_NAME_REQUIRED";
            } else if (errorMessage.contains("50文字以内で入力してください")) {
                errorCode = "CATEGORY_NAME_TOO_LONG";
            } else {
                errorCode = "VALIDATION_ERROR";
            }
        } else {
            errorCode = "VALIDATION_ERROR";
        }

        ErrorResponse errorResponse = new ErrorResponse(errorCode, errorMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 409 conflict: カテゴリ名の重複をハンドリング
    @ExceptionHandler(CategoryNameDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNameDuplicateException(CategoryNameDuplicateException e) {
        ErrorResponse errorResponse = new ErrorResponse("CATEGORY_NAME_DUPLICATE", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // 400 Bad Request: カテゴリの登録上限到達をハンドリング
    @ExceptionHandler(CategoryLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleCategoryLimitExceededException(CategoryLimitExceededException e) {
        ErrorResponse errorResponse = new ErrorResponse("CATEGORY_LIMIT_EXCEEDED", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
