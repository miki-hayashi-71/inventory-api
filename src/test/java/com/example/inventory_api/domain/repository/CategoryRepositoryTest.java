package com.example.inventory_api.domain.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.example.inventory_api.domain.model.Category;
import java.util.List;
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
   * findByNameAndUserIdAndDeletedFalse のテスト
   */
  @Test
  void findByNameAndUserIdAndDeletedFalse_未削除のカテゴリが存在する場合_そのカテゴリを返す() {
    // Arrange
    Category testCategory = new Category();
    testCategory.setName("テストカテゴリ");
    testCategory.setUserId(testUserId);
    testCategory.setDeleted(false);  // 削除されていない
    categoryRepository.save(testCategory);

    // Act
    List<Category> result = categoryRepository.findByNameAndUserIdAndDeletedFalse("テストカテゴリ",
        testUserId);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("テストカテゴリ");
  }

  @Test
  void findByNameAndUserIdAndDeletedFalse_削除済みのカテゴリしか存在しない場合_空のリストを返す() {
    // Arrange
    Category deletedCategory = new Category();
    deletedCategory.setName("テストカテゴリ");
    deletedCategory.setUserId(testUserId);
    deletedCategory.setDeleted(true); // 削除済み
    categoryRepository.save(deletedCategory);

    // Act
    List<Category> result = categoryRepository.findByNameAndUserIdAndDeletedFalse("テストカテゴリ",
        testUserId);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void findByUerIdAndDeletedFalse_指定したユーザーIDリストに紐づく未削除カテゴリのみを返す() {
    // Arrange
    // 1. テストユーザーのカテゴリ (取得対象)
    Category testUserCategory = new Category();
    testUserCategory.setName("カテゴリ1");
    testUserCategory.setUserId(testUserId);
    testUserCategory.setDeleted(false);
    categoryRepository.save(testUserCategory);

    // 2. システムユーザーのカテゴリ (取得対象)
    Category systemCategory = new Category();
    systemCategory.setName("カテゴリ2");
    systemCategory.setUserId(systemUserId);
    systemCategory.setDeleted(false);
    categoryRepository.save(systemCategory);

    // 3. テストユーザーの削除済みカテゴリ (取得対象外)
    Category deletedCategory = new Category();
    deletedCategory.setName("カテゴリ3");
    deletedCategory.setUserId(testUserId);
    deletedCategory.setDeleted(true);
    categoryRepository.save(deletedCategory);

    // 4. 別のユーザーのカテゴリ (取得対象外)
    Category anotherUserCategory = new Category();
    anotherUserCategory.setName("カテゴリ4");
    anotherUserCategory.setUserId(anotherUserId);
    anotherUserCategory.setDeleted(false);
    categoryRepository.save(anotherUserCategory);

    // Act
    List<String> userIdsToSearch = List.of(testUserId, systemUserId);
    List<Category> result = categoryRepository.findByUserIdInAndDeletedFalse(userIdsToSearch);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Category::getName)
        .containsExactlyInAnyOrder("カテゴリ1", "カテゴリ2");
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
