package com.example.inventory_api.controller;

import com.example.inventory_api.controller.advice.CustomExceptionHandler;
import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.controller.dto.CategoryResponse;
import com.example.inventory_api.controller.dto.CategoryUpdateRequest;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CategoryController.class, CustomExceptionHandler.class})
public class CategoryControllerTest {

  @Autowired
  private MockMvc mockmvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private CategoryService categoryService;

  /**
   * createCategory のテスト
   */
  @Test
  void createCategory_正常系_201Createdと作成されたカテゴリ情報を返す() throws Exception {
    // Arrange
    CategoryCreateRequest request = new CategoryCreateRequest();
    request.setName("テストカテゴリ");

    Category createdCategory = new Category();
    createdCategory.setId(1);
    createdCategory.setName("テストカテゴリ");

    when(categoryService.createCategory(any(), anyString()))
        .thenReturn(createdCategory);

    // Act & Assert
    mockmvc.perform(post("/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("テストカテゴリ"));
  }

  @Test
  void createCategory_カテゴリ名が空の場合_400BadRequestを返す() throws Exception {
    // Arrange
    CategoryCreateRequest request = new CategoryCreateRequest();
    request.setName(""); // 空のカテゴリ名を設定

    // Act & Assert
    mockmvc.perform(post("/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CATEGORY_NAME_REQUIRED"))
        .andExpect(jsonPath("$.message").value("カテゴリ名は必須です"));
  }

  @Test
  void createCategory_カテゴリ名が50文字を超える場合_400BadRequestを返す() throws Exception {
    // Arrange
    CategoryCreateRequest request = new CategoryCreateRequest();
    // 51文字の文字列を作成
    String longName = "a".repeat(51);
    request.setName(longName);

    // Act & Assert
    mockmvc.perform(post("/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CATEGORY_NAME_TOO_LONG"))
        .andExpect(jsonPath("$.message").value("カテゴリ名は50文字以内で入力してください"));
  }

  @Test
  void createCategory_カテゴリ名が重複する場合_409Conflictを返す() throws Exception {
    // Arrange
    CategoryCreateRequest request = new CategoryCreateRequest();
    request.setName("重複カテゴリ");

    when(categoryService.createCategory(any(), anyString()))
        .thenThrow(new IllegalStateException("DUPLICATE:そのカテゴリ名は既に使用されています"));

    // Act & Assert
    mockmvc.perform(post("/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CATEGORY_NAME_DUPLICATE"));
  }

  @Test
  void createCategory_登録上限に達している場合_400BadRequestを返す() throws Exception {
    // Arrange
    CategoryCreateRequest request = new CategoryCreateRequest();
    request.setName("新しいカテゴリ");

    // Serviceが上限エラーを投げるように設定
    when(categoryService.createCategory(any(), anyString()))
        .thenThrow(new IllegalStateException("LIMIT:登録できるカテゴリの上限に達しています"));

    // Act & Assert
    mockmvc.perform(post("/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CATEGORY_LIMIT_EXCEEDED"));
  }

  @Test
  void createCategory_DB保存失敗時にRuntimeExceptionが発生する場合_500InternalServerErrorを返す()
      throws Exception {
    // Arrange
    CategoryCreateRequest request = new CategoryCreateRequest();
    request.setName("テストカテゴリ");

    // ServiceがRuntimeExceptionをスローするよう設定
    when(categoryService.createCategory(any(), anyString()))
        .thenThrow(new RuntimeException("データベースへの保存に失敗しました"));

    // Act & Assert
    mockmvc.perform(post("/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError()) // 500エラーを期待
        .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
  }

  /**
   * getCategoryList のテスト
   */
  @Test
  void getCategoryList_正常系_200OKとカテゴリリストを返す() throws Exception {
    // Arrange ここでソートは行っていない
    List<CategoryResponse> categoryList = List.of(
        new CategoryResponse(1, "キッチン"),
        new CategoryResponse(2, "リビング")
    );
    when(categoryService.getCategoryList(anyString()))
        .thenReturn(categoryList);

    // Act & Assert
    mockmvc.perform(get("/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].name", is("キッチン")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].name", is("リビング")));
  }

  @Test
  void getCategoryList_カテゴリが存在しない場合_200OKと空のリストを返す() throws Exception {
    // Arrange
    when(categoryService.getCategoryList(anyString()))
        .thenReturn(Collections.emptyList());

    // Act & Assert
    mockmvc.perform(get("/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void getCategoryList_Service層で予期せぬエラーが発生した場合_500InternalServerErrorを返す()
      throws Exception {
    // Arrange
    when(categoryService.getCategoryList(anyString()))
        .thenThrow(new RuntimeException("予期せぬエラー"));

    // Act & Assert
    mockmvc.perform(get("/categories"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code", is("INTERNAL_SERVER_ERROR")))
        .andExpect(jsonPath("$.message", is("サーバー内部で予期せぬエラーが発生しました")));
  }

  /**
   * updateCategory のテスト
   */
  @Test
  void updateCategory_正常系_200OKと更新されたカテゴリ情報を返す() throws Exception {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("更新後カテゴリ");

    Category updatedCategory = new Category();
    updatedCategory.setId(1);
    updatedCategory.setName("更新後カテゴリ");

    when(categoryService.updateCategory(anyInt(), any(), anyString()))
        .thenReturn(updatedCategory);

    // Act & Assert
    mockmvc.perform(patch("/categories/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("更新後カテゴリ"));
  }

  @Test
  void updateCategory_カテゴリ名が空の場合_400BadRequestを返す() throws Exception {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName(""); // 空のカテゴリ名

    // Act & Assert
    mockmvc.perform(patch("/categories/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CATEGORY_NAME_REQUIRED"))
        .andExpect(jsonPath("$.message").value("カテゴリ名は必須です"));
  }

  @Test
  void updateCategory_カテゴリ名が50文字を超える場合_400BadRequestを返す() throws Exception {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    String longName = "a".repeat(51); // 51文字の文字列
    request.setName(longName);

    // Act & Assert
    mockmvc.perform(patch("/categories/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CATEGORY_NAME_TOO_LONG"))
        .andExpect(jsonPath("$.message").value("カテゴリ名は50文字以内で入力してください"));
  }

  @Test
  void updateCategory_更新対象が見つからない場合_404NotFoundを返す() throws Exception {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("更新カテゴリ");

    when(categoryService.updateCategory(anyInt(), any(), anyString()))
        .thenThrow(new IllegalStateException("NOT_FOUND:該当のカテゴリが見つかりません"));

    // Act & Assert
    mockmvc.perform(patch("/categories/999") // 存在しないID
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND_ERROR"));
  }

  @Test
  void updateCategory_デフォルトカテゴリを更新しようとした場合_403Forbiddenを返す()
      throws Exception {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("更新カテゴリ");

    when(categoryService.updateCategory(anyInt(), any(), anyString()))
        .thenThrow(new IllegalStateException("FORBIDDEN:このカテゴリを操作する権限がありません"));

    // Act & Assert
    mockmvc.perform(patch("/categories/1") // デフォルトカテゴリのIDを想定
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("DEFAULT_CATEGORY_IMMUTABLE"));
  }

  @Test
  void updateCategory_カテゴリ名が重複する場合_409Conflictを返す() throws Exception {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("重複カテゴリ");

    when(categoryService.updateCategory(anyInt(), any(), anyString()))
        .thenThrow(new IllegalStateException("DUPLICATE:そのカテゴリ名は既に使用されています"));

    // Act & Assert
    mockmvc.perform(patch("/categories/2")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CATEGORY_NAME_DUPLICATE"));
  }

  @Test
  void updateCategory_DB更新時に予期せぬエラーが発生した場合_500InternalServerErrorを返す()
      throws Exception {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("テストカテゴリ");

    when(categoryService.updateCategory(anyInt(), any(), anyString()))
        .thenThrow(new RuntimeException("データベースへのアクセスに失敗しました"));

    // Act & Assert
    mockmvc.perform(patch("/categories/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
  }
}
