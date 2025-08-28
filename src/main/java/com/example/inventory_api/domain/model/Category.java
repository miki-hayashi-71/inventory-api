package com.example.inventory_api.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String userId;

    private String name;

    private Boolean deleted;

    public Category(String userId,
                    String name,
                    Boolean deleted
    ) {
        this.userId = userId;
        this.name = name;
        this.deleted = deleted;
    }
}
