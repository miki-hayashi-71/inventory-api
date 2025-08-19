package com.example.inventory_api.controller;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.controller.dto.CategoryResponse;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@RequestBody @Validated CategoryCreateRequest request) {
        // TODO: 認証機能実装後、実際のuserIdに置き換える
        String temporaryUserId = "user1";

        Category createdCategory = categoryService.createCategory(request, temporaryUserId);

        CategoryResponse response = new CategoryResponse();
        response.setId(createdCategory.getId());
        response.setName(createdCategory.getName());

        return response;
    }
}
