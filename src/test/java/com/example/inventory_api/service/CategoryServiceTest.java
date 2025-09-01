package com.example.inventory_api.service;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.controller.dto.CategoryResponse;
import com.example.inventory_api.controller.dto.CategoryUpdateRequest;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
   * createCategory のテスト
   */
  @Test
  void createCategory_正常なリクエストの場合_カテゴリが作成される() {
    // Arrange
    CategoryCreateRequest request = new CategoryCreateRequest();
    request.setName("新しいカテゴリ");

    // 重複なし、上限未達
    when(categoryRepository.findUserCategories(anyString(), anyString()))
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

    Category existingCategory = new Category(1, testSystemUserId, "重複カテゴリ", false);

    when(categoryRepository.findUserCategories(anyString(), anyString()))
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
      fullCategoryList.add(
          new Category(i, testUserId, "カテゴリ" + i, false)
      );
    }

    // repositoryが50件のリストを返すよう設定
    when(categoryRepository.findUserCategories(anyString(), anyString()))
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
    for (int i = 0; i < 49; i++) {
      fullCategoryList.add(
          new Category(i, testUserId, "カテゴリ" + i, false)
      );
    }

    // repositoryが50件のリストを返すよう設定
    when(categoryRepository.findUserCategories(anyString(), anyString()))
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
    when(categoryRepository.findUserCategories(anyString(), anyString()))
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

  @Test
  void createCategory_DB検索時にNullPointerExceptionが発生する場合_RuntimeExceptionをスローする() {
    // Arrange
    CategoryCreateRequest request = new CategoryCreateRequest();
    request.setName("新しいカテゴリ");

    // DB検索時にNullPointerExceptionが発生するよう設定
    when(categoryRepository.findUserCategories(anyString(), anyString()))
        .thenThrow(new NullPointerException("テスト用のエラー"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      categoryService.createCategory(request, testUserId);
    });

    // メッセージの内容と、原因となった例外の型を検証
    assertThat(exception.getMessage()).isEqualTo("予期せぬエラーが発生しました");
    assertThat(exception.getCause()).isInstanceOf(NullPointerException.class);
  }

  @Test
  void createCategory_予期せぬExceptionが発生する場合_RuntimeExceptionをスローする() {
    // Arrange
    CategoryCreateRequest request = new CategoryCreateRequest();
    request.setName("新しいカテゴリ");

    // DB検索時に汎用的な例外が発生するよう設定
    when(categoryRepository.findUserCategories(anyString(), anyString()))
        .thenThrow(new RuntimeException("テスト用の予期せぬエラー"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      categoryService.createCategory(request, testUserId);
    });

    // メッセージの内容と、原因となった例外の型を検証
    assertThat(exception.getMessage()).isEqualTo("予期せぬエラーが発生しました");
  }

  /**
   * getCategoryList のテスト
   */
  @Test
  void getCategoryList_リポジトリから取得したカテゴリリストを辞書順で返す() {
    // Arrange
    List<Category> unsortedList = Arrays.asList(
        new Category(1, testUserId, "玄関", false),
        new Category(2, testUserId, "リビング・ダイニング", false),
        new Category(3, testUserId, "寝室", false),
        new Category(4, testUserId, "BathRoom", false),
        new Category(5, testUserId, "といれ", false)
    );

    when(categoryRepository.findUserCategories(testUserId, testSystemUserId)).
        thenReturn(unsortedList);

    // Act
    List<CategoryResponse> result = categoryService.getCategoryList(testUserId);

    // Assert
    assertThat(result).hasSize(5);

    assertThat(result.get(0).getName()).isEqualTo("BathRoom");
    assertThat(result.get(1).getName()).isEqualTo("といれ");
    assertThat(result.get(2).getName()).isEqualTo("リビング・ダイニング");
    assertThat(result.get(3).getName()).isEqualTo("玄関");
    assertThat(result.get(4).getName()).isEqualTo("寝室");

    assertThat(result.get(0).getId()).isEqualTo(4);
    assertThat(result.get(1).getId()).isEqualTo(5);
    assertThat(result.get(2).getId()).isEqualTo(2);
    assertThat(result.get(3).getId()).isEqualTo(1);
    assertThat(result.get(4).getId()).isEqualTo(3);

    verify(categoryRepository, times(1))
        .findUserCategories(testUserId, testSystemUserId);
  }

  @Test
  void getCategoryList_DB検索時にエラーが発生する場合_RuntimeExceptionをスローする() {
    // Arrange
    when(categoryRepository.findUserCategories(anyString(), anyString()))
        .thenThrow(new DataAccessResourceFailureException("DB接続エラー"));

    // Act & Assert
    assertThatThrownBy(() -> categoryService.getCategoryList(testUserId))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("データベースへのアクセスに失敗しました");
  }

  @Test
  void getCategoryList_予期せぬExceptionが発生する場合_RuntimeExceptionをスローする() {
    // Arrange
    when(categoryRepository.findUserCategories(anyString(), anyString()))
        .thenThrow(new RuntimeException("予期せぬエラー"));

    // Act & Assert
    assertThatThrownBy(() -> categoryService.getCategoryList(testUserId))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("予期せぬエラーが発生しました");
  }

  /**
   * updateCategory のテスト
   */
  @Test
  void updateCategory_正常系_カテゴリ名が更新される() {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("更新後の名前");

    Category originalCategory = new Category(1, testUserId, "元の名前", false);
    List<Category> categoryList = List.of(originalCategory);

    when(categoryRepository.findUserCategories(testUserId, testSystemUserId))
        .thenReturn(categoryList);
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Category result = categoryService.updateCategory(1, request, testUserId);

    // Assert
    assertThat(result.getName()).isEqualTo("更新後の名前");
    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  @Test
  void updateCategory_更新対象のカテゴリが存在しない場合_CategoryNotFoundExceptionをスローする() {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("更新後の名前");

    List<Category> emptyList = new ArrayList<>();
    when(categoryRepository.findUserCategories(testUserId, testSystemUserId))
        .thenReturn(emptyList);

    // Act & Assert
    assertThatThrownBy(() -> categoryService.updateCategory(999, request, testUserId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("NOT_FOUND");
  }

  @Test
  void updateCategory_別のユーザーのカテゴリを更新しようとした場合_ForbiddenExceptionをスローする() {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("更新後の名前");

    // testUserIdではないユーザーが所有するカテゴリ
    Category anotherUserCategory = new Category(2, "anotherUser", "他人のカテゴリ", false);
    List<Category> categoryList = List.of(anotherUserCategory);

    when(categoryRepository.findUserCategories(testUserId, testSystemUserId))
        .thenReturn(categoryList);

    // Act & Assert
    assertThatThrownBy(() -> categoryService.updateCategory(2, request, testUserId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("FORBIDDEN");
  }

  @Test
  void updateCategory_デフォルトカテゴリを更新しようとした場合_ForbiddenExceptionをスローする() {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("更新後の名前");

    Category defaultCategory = new Category(1, testSystemUserId, "デフォルトカテゴリ", false);
    List<Category> categoryList = new ArrayList<>(List.of(defaultCategory));

    when(categoryRepository.findUserCategories(testUserId, testSystemUserId))
        .thenReturn(categoryList);

    // Act & Assert
    assertThatThrownBy(() -> categoryService.updateCategory(1, request, testUserId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("FORBIDDEN");
  }

  @Test
  void updateCategory_カテゴリ名が既存のカテゴリと重複する場合_CategoryNameDuplicateExceptionをスローする() {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("既存の名前");

    Category categoryToUpdate = new Category(1, testUserId, "元の名前", false);
    Category existingCategory = new Category(2, testUserId, "既存の名前", false);
    List<Category> categoryList = List.of(categoryToUpdate, existingCategory);

    when(categoryRepository.findUserCategories(testUserId, testSystemUserId))
        .thenReturn(categoryList);

    // Act & Assert
    assertThatThrownBy(() -> categoryService.updateCategory(1, request, testUserId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("DUPLICATE");
  }

  @Test
  void updateCategory_DB保存時にDataAccessExceptionが発生する場合_RuntimeExceptionをスローする() {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("更新後の名前");

    Category originalCategory = new Category(1, testUserId, "元の名前", false);
    List<Category> categoryList = new ArrayList<>(List.of(originalCategory));

    when(categoryRepository.findUserCategories(testUserId, testSystemUserId))
        .thenReturn(categoryList);
    when(categoryRepository.save(any(Category.class)))
        .thenThrow(new DataAccessResourceFailureException("DB接続エラー"));

    // Act & Assert
    assertThatThrownBy(() -> categoryService.updateCategory(1, request, testUserId))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("データベースへのアクセスに失敗しました");
  }

  @Test
  void updateCategory_DB保存時にNullPointerExceptionが発生する場合_RuntimeExceptionをスローする() {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("更新後の名前");

    Category originalCategory = new Category(1, testUserId, "元の名前", false);
    List<Category> categoryList = new ArrayList<>(List.of(originalCategory));

    when(categoryRepository.findUserCategories(testUserId, testSystemUserId))
        .thenReturn(categoryList);
    when(categoryRepository.save(any(Category.class)))
        .thenThrow(new NullPointerException("テスト用のエラー"));

    // Act & Assert
    assertThatThrownBy(() -> categoryService.updateCategory(1, request, testUserId))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("予期せぬエラーが発生しました")
        .hasCauseInstanceOf(NullPointerException.class);
  }

  @Test
  void updateCategory_予期せぬExceptionが発生する場合_RuntimeExceptionをスローする() {
    // Arrange
    CategoryUpdateRequest request = new CategoryUpdateRequest();
    request.setName("更新後の名前");

    Category originalCategory = new Category(1, testUserId, "元の名前", false);
    List<Category> categoryList = new ArrayList<>(List.of(originalCategory));

    when(categoryRepository.findUserCategories(testUserId, testSystemUserId))
        .thenReturn(categoryList);
    when(categoryRepository.save(any(Category.class)))
        .thenThrow(new RuntimeException("テスト用の予期せぬエラー"));

    // Act & Assert
    assertThatThrownBy(() -> categoryService.updateCategory(1, request, testUserId))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("予期せぬエラーが発生しました");
  }

}
