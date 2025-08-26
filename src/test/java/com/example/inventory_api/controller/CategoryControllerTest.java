package com.example.inventory_api.controller;

import com.example.inventory_api.controller.advice.CustomExceptionHandler;
import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
     * POST /categories のテスト
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
    void createCategory_DB保存失敗時にRuntimeExceptionが発生する場合_500InternalServerErrorを返す() throws Exception {
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
     * GET /categories のテスト
     */
//    @Test
//    void getCategoryList_正常系_200OKとカテゴリリストを返す() throws Exception {
//        // Arrange
//        Category category1 = new Category();
//        category1.setId(1);
//        category1.setName("テストカテゴリ1");
//
//        Category category2 = new Category();
//        category2.setId(2);
//        category2.setName("テストカテゴリ2");
//
//        List<Category> categoryList = List.of(category1, category2);
//
//        when(categoryService.getCategoryList(anyString()))
//                .thenReturn(categoryList);
//
//        // Act & Assert
//        mockmvc.perform(get("/categories"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].id").value(1))
//                .andExpect(jsonPath("$[0].name").value("テストカテゴリ1"))
//                .andExpect(jsonPath("$[1].id").value(2))
//                .andExpect(jsonPath("$[1].name").value("テストカテゴリ2"));
//    }
}
