package com.example.inventory_api.controller.advice;

import com.example.inventory_api.controller.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice // 全ての@RestControllerに対する共通処理を担うクラスであることを宣言
public class CustomExceptionHandler {

    // Service層で定義するエラーメッセージの接頭辞
    private static final String DUPLICATE_PREFIX = "DUPLICATE:";
    private static final String LIMIT_PREFIX = "LIMIT:";
    private static final String NOT_FOUND_PREFIX = "NOT_FOUND:";
    private static final String FORBIDDEN_PREFIX = "FORBIDDEN:";

    // エラーメッセージを定数化
    private static final String MSG_VALIDATION_ERROR = "不正なリクエストです";
    private static final String MSG_INTERNAL_SERVER_ERROR = "サーバー内部で予期せぬエラーが発生しました";
    private static final String MSG_BAD_REQUEST = "不正なリクエストです";
    private static final String MSG_NOT_FOUND = "指定されたリソースが見つかりません";

    // DTOのバリデーションメッセージに含まれる部分文字列
    private static final String PARTIAL_MSG_REQUIRED = "必須です";
    private static final String PARTIAL_MSG_TOO_LONG = "50文字以内で入力してください";

    // 404 Not Found
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoHandlerFoundException(NoHandlerFoundException e) {
        return new ErrorResponse("NOT_FOUND", MSG_NOT_FOUND);
    }

    // 400 Bad Request: 入力値のチェック
    @ExceptionHandler(MethodArgumentNotValidException.class) // DTOのバリデーションで拾えるもの
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        // 最初に見つかったエラーを取得
        FieldError fieldError = e.getBindingResult().getFieldError();

        if (fieldError == null) {
            ErrorResponse errorResponse = new ErrorResponse("VALIDATION_ERROR", MSG_VALIDATION_ERROR);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        String errorMessage = fieldError.getDefaultMessage();
        String errorCode;

        // エラーメッセージの内容に応じて、API仕様書のエラーコードを判定
        if (errorMessage != null) {
            if (errorMessage.contains(PARTIAL_MSG_REQUIRED)) {
                errorCode = "CATEGORY_NAME_REQUIRED";
            } else if (errorMessage.contains(PARTIAL_MSG_TOO_LONG)) {
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

    // 400 or 409: Service層で発生したエラーをハンドリング
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        String message = e.getMessage();
        if(message.startsWith(DUPLICATE_PREFIX)) {
            ErrorResponse errorResponse = new ErrorResponse("CATEGORY_NAME_DUPLICATE", message.substring(DUPLICATE_PREFIX.length()));
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT); // 409
        }
        if (message.startsWith(LIMIT_PREFIX)) {
            ErrorResponse errorResponse = new ErrorResponse("CATEGORY_LIMIT_EXCEEDED", message.substring(LIMIT_PREFIX.length()));
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); // 400
        }
        if (message.startsWith(NOT_FOUND_PREFIX)) {
            ErrorResponse errorResponse = new ErrorResponse("NOT_FOUND_ERROR", message.substring(10));
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND); // 404
        }
        if (message.startsWith(FORBIDDEN_PREFIX)) {
            ErrorResponse errorResponse = new ErrorResponse("DEFAULT_CATEGORY_IMMUTABLE", message.substring(10));
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN); // 403
        }

        // その他のIllegalStateExceptionは汎用的な400エラーとして返す
        ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST", MSG_BAD_REQUEST);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); // 400
    }

    // 500 Internal Server Error: 予期せぬエラー全般をハンドリング
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse("INTERNAL_SERVER_ERROR", MSG_INTERNAL_SERVER_ERROR);
        // 本番環境ではここで詳細なエラーログを出力することが重要
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
