package com.example.inventory_api.controller;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.controller.dto.CategoryResponse;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@RequestBody @Validated CategoryCreateRequest request) {
        // TODO: 認証機能実装後、実際のuserIdに置き換える
        String currentUserId = "user1";

        Category createdCategory = categoryService.createCategory(request, currentUserId);

        CategoryResponse response = new CategoryResponse();
        response.setId(createdCategory.getId());
        response.setName(createdCategory.getName());

        return response;
    }

    /**
     * 指定されたIDのカスタムカテゴリを1件論理削除するAPI
     * DELETE /categories/{categoryId}
     */
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 成功時は204 No Contentを返す
    public void deleteCategory(@PathVariable Integer categoryId) {
        // TODO: 認証機能実装後、実際のuserIdに置き換える
        String currentUserId = "user1";

        categoryService.deleteCategory(categoryId, currentUserId);
    }
}
