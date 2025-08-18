package com.example.inventory_api.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "items")
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

}
