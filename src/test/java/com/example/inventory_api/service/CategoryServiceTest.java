package com.example.inventory_api.service;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.repository.CategoryRepository;
import com.example.inventory_api.service.exception.CategoryLimitExceededException;
import com.example.inventory_api.service.exception.CategoryNameDuplicateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

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
    private final String systemUserId = "system";

    @Test
    void createCategory_正常なリクエストの場合_カテゴリが作成される() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("新しいカテゴリ");

        when(categoryRepository.existsByNameAndUserIdInAndDeletedFalse(anyString(), any(List.class)))
                .thenReturn(false);  // 重複なし
        when(categoryRepository.countByUserIdAndDeletedFalse(testUserId))
                .thenReturn(10L);
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
        request.setName("重複カテゴリ");
        when(categoryRepository.existsByNameAndUserIdInAndDeletedFalse(anyString(), any(List.class)))
                .thenReturn(true); // 重複あり

        // Act & Assert
        assertThrows(CategoryNameDuplicateException.class, () -> {
            categoryService.createCategory(request, testUserId);
        });
    }

    @Test
    void createCategory_登録上限に達している場合_CategoryLimitExceededExceptionをスローする() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("新しいカテゴリ");
        when(categoryRepository.countByUserIdAndDeletedFalse(testUserId))
                .thenReturn(50L);

        // Act and Assert
        assertThrows(CategoryLimitExceededException.class, () -> {
            categoryService.createCategory(request, testUserId);
        });
    }
}

