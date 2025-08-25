package com.example.inventory_api.service;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.util.ReflectionTestUtils;

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
    private final String testSystemUserId = "system";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(categoryService, "maxCustomCategoryLimit", 50);
    }

    /**
     * POST /categories のテスト
     */
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
        List<Category> fullCategoryList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Category c = new Category();
            c.setUserId(testUserId);
            c.setName("カテゴリ" + i);
            fullCategoryList.add(c);
        }

        // repositoryが50件のリストを返すよう設定
        when(categoryRepository.findByUserIdInAndDeletedFalse(any(List.class)))
                .thenReturn(fullCategoryList);

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> {
            categoryService.createCategory(request, testUserId);
        });
        // saveが呼ばれないことを確認
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_登録済みカテゴリ数が49件の場合_50件目のカテゴリが作成できる() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("50個目のカスタムカテゴリ");

        // 50件登録済みのリストを作成
        List<Category> fullCategoryList = new ArrayList<>();
        for (int i = 0; i < 29; i++) {
            Category c = new Category();
            c.setUserId(testUserId);
            c.setName("カテゴリ" + i);
            fullCategoryList.add(c);
        }

        // repositoryが50件のリストを返すよう設定
        when(categoryRepository.findByUserIdInAndDeletedFalse(any(List.class)))
                .thenReturn(fullCategoryList);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Category result = categoryService.createCategory(request, testUserId);

        // Assert
        // 例外がスローされず、カテゴリが正しく作成されたことを確認
        assertThat(result.getName()).isEqualTo("50個目のカスタムカテゴリ");
        // saveが1回呼ばれたことを確認
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_DB保存時にエラーが発生する場合_RuntimeExceptionをスローする() {
        // Arrange
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("新しいカテゴリ");

        // 重複なし、上限未達
        when(categoryRepository.findByUserIdInAndDeletedFalse(any(List.class)))
                .thenReturn(new ArrayList<>());

        // repository.save()が呼ばれたら、DataAccessExceptionをスローするよう設定
        when(categoryRepository.save(any(Category.class)))
                .thenThrow(new DataAccessResourceFailureException("DB接続エラー"));

        // Act & Assert
        // ServiceがRuntimeExceptionをスローすることを確認
        assertThrows(RuntimeException.class, () -> {
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

