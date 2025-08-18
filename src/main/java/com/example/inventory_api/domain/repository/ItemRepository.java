package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Integer> {

}
