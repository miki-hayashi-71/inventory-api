package com.example.inventory_api.domain.model;

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

  private Integer price;

  private String place;

  private Boolean deleted;

  private LocalDateTime created;

  public Item(
      Integer categoryId,
      String userId,
      String name,
      Integer quantity,
      Boolean deleted
  ) {
    this.categoryId = categoryId;
    this.userId = userId;
    this.name = name;
    this.quantity = quantity;
    this.deleted = deleted;
  }
}
