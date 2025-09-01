package com.example.inventory_api.domain.model;

import com.example.inventory_api.controller.dto.ItemCreateRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "items")
@NoArgsConstructor
@AllArgsConstructor
public class Item {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer categoryId;

  private String userId;

  private String name;

  private Integer quantity;

  private Integer amount;

  private String place;

  private Boolean deleted;

  private LocalDateTime created;

  public Item(ItemCreateRequest request, String userId) {
    this.categoryId = request.getCategoryId();
    this.userId = userId;
    this.name = request.getName();
    this.quantity = request.getQuantity();
    this.amount = request.getAmount();
    this.place = request.getPlace();
    this.deleted = false;
    this.created = LocalDateTime.now();
  }
}
