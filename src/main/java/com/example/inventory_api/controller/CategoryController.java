package com.example.inventory_api.controller;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.controller.dto.CategoryResponse;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 新しいカスタムカテゴリを1件登録するAPI
     * POST /categories
     */
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
     * カテゴリ一覧を取得するAPI
     * GET /categories
     */
    @GetMapping
    public List<CategoryResponse> getCategoryList() {
        //  TODO:認証機能実装後、実際のuserIdに置き換える
        String currentUserId = "user1";

        List<Category> categories = categoryService.getCategoryList(currentUserId);
        return categories.stream()
                .map(category -> {
                    CategoryResponse res = new CategoryResponse();
                    res.setId(category.getId());
                    res.setName(category.getName());
                    return res;
                })
                .collect(Collectors.toList());
    }
}
