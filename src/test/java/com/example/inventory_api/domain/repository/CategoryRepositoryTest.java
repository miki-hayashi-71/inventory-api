package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    // テスト用のユーザーID
    private final String testUserId = "user1";
    private final String anotherUserId = "user2";
    private final String systemUserId = "system";

    /**
     * POST /categories のテスト
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
        List<Category> result = categoryRepository.findByNameAndUserIdAndDeletedFalse("テストカテゴリ", testUserId);

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
        List<Category> result = categoryRepository.findByNameAndUserIdAndDeletedFalse("テストカテゴリ", testUserId);

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
        assertThat(result).hasSize(2); // c1 と c2 の2件が取得されるはず
        assertThat(result).extracting(Category::getName).containsExactlyInAnyOrder("カテゴリ1", "カテゴリ2");
    }

    /**
     * GET /categories のテスト
     */
//    @Test
//    void findByUserIdAndDeletedFalseOrUserIdAndDeletedFalse_カスタムカテゴリとデフォルトカテゴリを併せて名前順に取得する() {
//        // Arrange
//        String testUserId = "user1";
//        String testSystemUserId = "system";
//
//        // テストデータの作成
//        Category custumCategory = new Category();
//        custumCategory.setName("カスタムカテゴリ");
//        custumCategory.setUserId(testUserId);
//        custumCategory.setDeleted(false);
//        categoryRepository.save(custumCategory);
//
//        Category defaultCategory = new Category();
//        defaultCategory.setName("デフォルトカテゴリ");
//        defaultCategory.setUserId(testSystemUserId); // システムユーザーが作成
//        defaultCategory.setDeleted(false);
//        categoryRepository.save(defaultCategory);
//
//        Category deletedCategory = new Category();
//        deletedCategory.setName("削除済みカテゴリ");
//        deletedCategory.setUserId(testUserId);
//        deletedCategory.setDeleted(true); // 削除済みのカテゴリ
//        categoryRepository.save(deletedCategory);
//
//        // Act
//        List<Category> result = categoryRepository.findByUserIdAndDeletedFalseOrUserIdAndDeletedFalse(testUserId, testSystemUserId);
//
//        // Assert
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0).getName()).isEqualTo("カスタムカテゴリ"); // 「カ」が「デ」より先に表示される
//        assertThat(result.get(1).getName()).isEqualTo("デフォルトカテゴリ");
//    }
}
