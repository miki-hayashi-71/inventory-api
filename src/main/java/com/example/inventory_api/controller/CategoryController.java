package com.example.inventory_api.controller;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.controller.dto.CategoryResponse;
import com.example.inventory_api.controller.dto.CategoryUpdateRequest;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  /**
   * 新しいカスタムカテゴリを1件登録するAPI POST /categories
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryResponse createCategory(@RequestBody @Validated CategoryCreateRequest request) {
    // TODO: 認証機能実装後、実際のuserIdに置き換える
    String currentUserId = "user1";

    Category createdCategory = categoryService.createCategory(request, currentUserId);

    return new CategoryResponse(createdCategory);
  }

  /**
   * カテゴリ一覧を取得するAPI GET /categories
   */
  @GetMapping
  public List<CategoryResponse> getCategoryList() {
    //  TODO:認証機能実装後、実際のuserIdに置き換える
    String currentUserId = "user1";

    // serviceからResponseのリストを受け取って返す
    return categoryService.getCategoryList(currentUserId);
  }

  /**
   * カスタムカテゴリを1件更新するAPI PATCH /categories/{categoryId}
   */
  @PatchMapping("/{categoryId}")
  public CategoryResponse updateCategory(@PathVariable Integer categoryId,
      @RequestBody @Validated CategoryUpdateRequest request) {
    // TODO: 認証機能実装後、実際のuserIdに置き換える
    String currentUserId = "user1";

    Category updatedCategory = categoryService.updateCategory(categoryId, request, currentUserId);

    return new CategoryResponse(updatedCategory);
  }

  /**
   * 指定されたIDのカスタムカテゴリを1件論理削除するAPI DELETE /categories/{categoryId}
   */
  @DeleteMapping("/{categoryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT) // 成功時は204 No Contentを返す
  public void deleteCategory(@PathVariable Integer categoryId) {
    // TODO: 認証機能実装後、実際のuserIdに置き換える
    String currentUserId = "user1";

    categoryService.deleteCategory(categoryId, currentUserId);
  }
}
