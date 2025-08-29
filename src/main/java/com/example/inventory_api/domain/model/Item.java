package com.example.inventory_api.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    public Item(Integer categoryId, String userId, String name, Integer quantity, Boolean deleted) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.name = name;
        this.quantity = quantity;
        this.deleted = deleted;
    }
}
