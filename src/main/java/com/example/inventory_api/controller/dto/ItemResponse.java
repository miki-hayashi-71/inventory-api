package com.example.inventory_api.controller.dto;

import com.example.inventory_api.domain.model.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

  private Integer id;
  private Integer categoryId;
  private String name;
  private Integer quantity;

  public ItemResponse(Item item) {
    this.id = item.getId();
    this.categoryId = item.getCategoryId();
    this.name = item.getName();
    this.quantity = item.getQuantity();
  }
}
