package com.example.inventory_api.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryCreateRequest {

  @Pattern(regexp = ".*[^\\s　].*", message = "カテゴリ名は必須です")
  @NotBlank(message = "カテゴリ名は必須です")
  @Size(max = 50, message = "カテゴリ名は50文字以内で入力してください")
  private String name;
}
