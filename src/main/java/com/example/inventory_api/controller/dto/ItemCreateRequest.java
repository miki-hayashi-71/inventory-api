package com.example.inventory_api.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemCreateRequest {

  @NotNull(message = "カテゴリIDは必須です")
  private Integer categoryId;

  @Pattern(regexp = ".*[^\\s　].*", message = "アイテム名は必須です")
  @NotBlank(message = "アイテム名は必須です")
  @Size(max = 50, message = "アイテム名は50文字以内で入力してください")
  private String name;

  @Min(value = 0, message = "数量は0以上の整数で入力してください")
  private Integer quantity = 0;

  private Integer amount;

  private String place;
}
