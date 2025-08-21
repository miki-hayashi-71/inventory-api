package com.example.inventory_api.service;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @InjectMocks // テスト対象のクラス
    private CategoryService categoryService;

    @Mock // モックにするクラス
    private CategoryRepository categoryRepository;

    private final String testUserId = "user1";

    @Test
    void createCategory_正常なリクエストの場合_カテゴリが作成される() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("新しいカテゴリ");

        // 重複なし、上限未達
        when(categoryRepository.findByUserIdInAndDeletedFalse(any(List.class)))
                .thenReturn(new ArrayList<>());
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Category result = categoryService.createCategory(request, testUserId);

        // Assert
        assertThat(result.getName()).isEqualTo("新しいカテゴリ");
        assertThat(result.getUserId()).isEqualTo(testUserId);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_カテゴリ名が重複する場合_CategoryNameDuplicateExceptionをスローする() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("重複カテゴリ"); // 作りたいカテゴリ

        Category existingCategory = new Category();
        existingCategory.setName("重複カテゴリ"); // 既に存在するカテゴリ

        when(categoryRepository.findByUserIdInAndDeletedFalse(any(List.class)))
                .thenReturn(List.of(existingCategory));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            categoryService.createCategory(request, testUserId);
        });
        // saveが呼ばれないことを確認
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_登録上限に達している場合_CategoryLimitExceededExceptionをスローする() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("51個目のカスタムカテゴリ");

        // 50件登録済みのリストを作成
        List<Category> fullList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Category c = new Category();
            c.setUserId(testUserId);
            c.setName("カテゴリ" + i);
            fullList.add(c);
        }

        // repositoryが50件のリストを返すよう設定
        when(categoryRepository.findByUserIdInAndDeletedFalse(any(List.class)))
                .thenReturn(fullList);

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> {
            categoryService.createCategory(request, testUserId);
        });
        // saveが呼ばれないことを確認
        verify(categoryRepository, never()).save(any(Category.class));
    }
}

