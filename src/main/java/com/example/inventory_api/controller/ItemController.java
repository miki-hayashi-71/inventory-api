package com.example.inventory_api.controller;

import com.example.inventory_api.controller.dto.ItemCreateRequest;
import com.example.inventory_api.controller.dto.ItemResponse;
import com.example.inventory_api.domain.model.Item;
import com.example.inventory_api.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ItemResponse createItem(@RequestBody @Validated ItemCreateRequest request) {
    // TODO: 認証機能実装後、実際のuserIdに置き換える
    String currentUserId = "user1";

    Item createdItem = itemService.createItem(request, currentUserId);

    return new ItemResponse(createdItem);
  }
}
