package com.example.inventory_api.domain.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.example.inventory_api.domain.model.Category;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class CategoryRepositoryTest {

  @Autowired
  private CategoryRepository categoryRepository;

  // テスト用のユーザーID
  private final String testUserId = "user1";
  private final String anotherUserId = "user2";
  private final String systemUserId = "system";

  /**
   * duplicateCheck のテスト
   */
  @Test
  void duplicateCheck_未削除のカテゴリが存在する場合_そのカテゴリを含むOptionalを返す() {
    // Arrange
    Category testCategory = new Category(testUserId, "テストカテゴリ", false);
    categoryRepository.save(testCategory);

    // Act
    Optional<Category> result = categoryRepository.findExistingCategories("テストカテゴリ", testUserId);

    // Assert
    assertThat(result).isPresent(); // Optionalが空でないことを確認
    assertThat(result.get().getName()).isEqualTo("テストカテゴリ");
  }

  @Test
  void duplicateCheck_削除済みのカテゴリしか存在しない場合_空のOptionalを返す() {
    // Arrange
    Category deletedCategory = new Category(testUserId, "テストカテゴリ", true);
    categoryRepository.save(deletedCategory);

    // Act
    Optional<Category> result = categoryRepository.findExistingCategories("テストカテゴリ", testUserId);

    // Assert
    assertThat(result).isEmpty();
  }

  /**
   * findUserCategories のテスト
   */
  @Test
  void findUserCategories_カスタムカテゴリとデフォルトカテゴリを併せて取得する() {
    // Arrange
    categoryRepository.save(new Category(testUserId, "カスタムカテゴリ", false));
    categoryRepository.save(new Category(systemUserId, "デフォルトカテゴリ", false));
    categoryRepository.save(new Category(testUserId, "削除済みカテゴリ", true));
    categoryRepository.save(new Category(anotherUserId, "他のユーザーのカテゴリ", false));

    // Act
    List<Category> result = categoryRepository.findUserCategories(testUserId, systemUserId);

    // Assert ここではソートは求めない
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Category::getName)
        .containsExactlyInAnyOrder("カスタムカテゴリ", "デフォルトカテゴリ");
  }

  @Test
  void findUserCategories_該当カテゴリが存在しない場合_空のリストを返す() {
    // Arrange
    categoryRepository.save(new Category(anotherUserId, "他のユーザーのカテゴリ", false));
    categoryRepository.save(new Category(testUserId, "検索対象ユーザーの削除済みカテゴリ", true));

    // Act
    List<Category> result = categoryRepository.findUserCategories(testUserId, systemUserId);

    // Assert  nullではなく、要素数0のリストを確認
    assertThat(result).isNotNull().isEmpty();
  }
}
