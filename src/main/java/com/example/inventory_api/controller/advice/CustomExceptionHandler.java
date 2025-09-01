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
  private static final String CONFLICT_PREFIX = "CONFLICT:";

  // エラーメッセージを定数化
  private static final String MSG_VALIDATION_ERROR = "不正なリクエストです";
  private static final String MSG_INTERNAL_SERVER_ERROR = "サーバー内部で予期せぬエラーが発生しました";
  private static final String MSG_BAD_REQUEST = "不正なリクエストです";
  private static final String MSG_NOT_FOUND = "指定されたリソースが見つかりません";

  // DTOのバリデーションメッセージに含まれる部分文字列
  private static final String PARTIAL_MSG_REQUIRED = "必須です";
  private static final String PARTIAL_MSG_TOO_LONG = "50文字以内で入力してください";
  private static final String PARTIAL_MSG_QUANTITY = "0以上の整数";

  // 404 Not Found
  @ExceptionHandler(NoHandlerFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleNoHandlerFoundException(NoHandlerFoundException e) {
    return new ErrorResponse("NOT_FOUND", MSG_NOT_FOUND);
  }

  // 400 Bad Request: 入力値のチェック
  @ExceptionHandler(MethodArgumentNotValidException.class) // DTOのバリデーションで拾えるもの
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException e) {
    // 最初に見つかったエラーを取得
    FieldError fieldError = e.getBindingResult().getFieldError();

    if (fieldError == null) {
      return new ResponseEntity<>(
          new ErrorResponse("VALIDATION_ERROR", MSG_VALIDATION_ERROR),
          HttpStatus.BAD_REQUEST
      );
    }

    String fieldName = fieldError.getField();
    String defaultMessage = fieldError.getDefaultMessage();
    String errorCode = "VALIDATION_ERROR";

    switch (fieldName) {
      case "categoryId":
      case "name":
        if (defaultMessage != null && defaultMessage.contains(PARTIAL_MSG_REQUIRED)) {
          errorCode = "ITEM_FIELDS_REQUIRED";
        } else if (defaultMessage != null && defaultMessage.contains(PARTIAL_MSG_TOO_LONG)) {
          errorCode = "ITEM_NAME_TOO_LONG";
        }
        break;
      case "quantity":
        if (defaultMessage != null && defaultMessage.contains(PARTIAL_MSG_QUANTITY)) {
          errorCode = "INVALID_ITEM_QUANTITY";
        }
        break;
      default:
        // ★★★ ここの判定を修正しました ★★★
        if (defaultMessage != null) {
          if (defaultMessage.contains(PARTIAL_MSG_REQUIRED)) {
            errorCode = "CATEGORY_NAME_REQUIRED";
          } else if (defaultMessage.contains(PARTIAL_MSG_TOO_LONG)) {
            errorCode = "CATEGORY_NAME_TOO_LONG";
          }
        }
        break;
    }

    ErrorResponse errorResponse = new ErrorResponse(errorCode, defaultMessage);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // Service層で発生したエラーをハンドリング
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
    String message = e.getMessage();

    if (message.startsWith(CONFLICT_PREFIX)) {
      return new ResponseEntity<>(
          new ErrorResponse(
              "ITEM_NAME_DUPLICATE",
              message.substring(CONFLICT_PREFIX.length())
          ),
          HttpStatus.CONFLICT
      );
    }
    if (message.startsWith(DUPLICATE_PREFIX)) {
      return new ResponseEntity<>(
          new ErrorResponse(
              "CATEGORY_NAME_DUPLICATE",
              message.substring(DUPLICATE_PREFIX.length())
          ),
          HttpStatus.CONFLICT
      );
    }
    if (message.startsWith(LIMIT_PREFIX)) {
      return new ResponseEntity<>(
          new ErrorResponse(
              "CATEGORY_LIMIT_EXCEEDED",
              message.substring(LIMIT_PREFIX.length())
          ),
          HttpStatus.BAD_REQUEST
      );
    }
    if (message.startsWith(NOT_FOUND_PREFIX)) {
      return new ResponseEntity<>(
          new ErrorResponse(
              "CATEGORY_NOT_FOUND",
              message.substring(NOT_FOUND_PREFIX.length())
          ),
          HttpStatus.NOT_FOUND
      );
    }
    if (message.startsWith(FORBIDDEN_PREFIX)) {
      return new ResponseEntity<>(
          new ErrorResponse(
              "DEFAULT_CATEGORY_IMMUTABLE",
              message.substring(FORBIDDEN_PREFIX.length())
          ),
          HttpStatus.FORBIDDEN
      );
    }

    return new ResponseEntity<>(
        new ErrorResponse(
            "BAD_REQUEST",
            MSG_BAD_REQUEST
        ),
        HttpStatus.BAD_REQUEST);
  }

  // 500 Internal Server Error: 予期せぬエラー全般をハンドリング
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception e) {
    return new ResponseEntity<>(
        new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            MSG_INTERNAL_SERVER_ERROR
        ),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
