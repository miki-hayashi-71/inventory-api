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

import java.util.ArrayList;
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
    private final String testSystemUserId = "system";

    /**
     * POST /categories のテスト
     */
    @Test
    void createCategory_正常なリクエストの場合_カテゴリが作成される() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("新しいカテゴリ");

        when(categoryRepository.findByNameAndUserIdAndDeletedFalse("新しいカテゴリ", testUserId))
                .thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIdAndDeletedFalse("新しいカテゴリ", testSystemUserId))
                .thenReturn(Optional.empty());
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
        when(categoryRepository.findByNameAndUserIdAndDeletedFalse("重複カテゴリ", testUserId))
                .thenReturn(Optional.of(new Category()));

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

    /**
     * GET /categories のテスト
     */
    @Test
    void getCategoryList_リポジトリから取得したカテゴリリストを辞書順で返す() {
        // Arrange
        String testUserId = "user1";
        String testSystemUserId = "system";

        // 辞書順ではないリストを作成
        Category category1 = new Category();
        category1.setName("玄関");

        Category category2 = new Category();
        category2.setName("リビング・ダイニング");

        Category category3 = new Category();
        category3.setName("寝室");

        Category category4 = new Category();
        category4.setName("BathRoom");

        Category category5 = new Category();
        category5.setName("といれ");

        List<Category> unsortedList = new ArrayList<>();
        unsortedList.add(category1);
        unsortedList.add(category2);
        unsortedList.add(category3);
        unsortedList.add(category4);
        unsortedList.add(category5);

        when(categoryRepository.findByUserIdAndDeletedFalseOrUserIdAndDeletedFalse(testUserId, testSystemUserId))
                .thenReturn(unsortedList);

        // Act
        List<Category> result = categoryService.getCategoryList(testUserId);

        // Assert
        assertThat(result).hasSize(5);
        assertThat(result.get(0).getName()).isEqualTo("BathRoom"); // アルファベット「B」
        assertThat(result.get(1).getName()).isEqualTo("といれ"); // ひらがな・カタカナ「と」
        assertThat(result.get(2).getName()).isEqualTo("リビング・ダイニング"); // ひらがな・カタカナ「リ」
        assertThat(result.get(3).getName()).isEqualTo("玄関"); // 漢字「読み：げ」
        assertThat(result.get(4).getName()).isEqualTo("寝室"); // 漢字「読み：し」

        verify(categoryRepository, times(1))
                .findByUserIdAndDeletedFalseOrUserIdAndDeletedFalse(testUserId, testSystemUserId);
    }
}

