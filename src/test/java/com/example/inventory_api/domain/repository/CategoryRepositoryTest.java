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

    /**
     * POST /categories のテスト
     */
    @Test
    void findByNameAndUserId_指定した名称とユーザーIDのカテゴリが存在する場合_そのカテゴリを返す() {
        // Arrange
        Category category = new Category();
        category.setName("テストカテゴリ");
        category.setUserId(testUserId);
        category.setDeleted(false);
        categoryRepository.save(category);

        // Act
        List<Category> result = categoryRepository.findByNameAndUserId("テストカテゴリ", testUserId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("テストカテゴリ");
    }

    @Test
    void findByNameAndUserIdAndDeletedFalse_未削除のカテゴリが存在する場合_そのカテゴリを返す() {
        // Arrange
        Category category = new Category();
        category.setName("テストカテゴリ");
        category.setUserId(testUserId);
        category.setDeleted(false);
        categoryRepository.save(category);

        // Act
        var result = categoryRepository.findByNameAndUserIdAndDeletedFalse("テストカテゴリ", testUserId);

        // Assert
        assertThat(result).isPresent();
    }

    @Test
    void countByUserIdAndDeletedFalse_指定したユーザーの未削除のカテゴリ数を正しくカウントする() {
        // Arrange
        Category c1 = new Category();
        c1.setName("カテゴリ1");
        c1.setUserId(testUserId);
        c1.setDeleted(false);
        categoryRepository.save(c1);

        Category c2 = new Category();
        c2.setName("カテゴリ2");
        c2.setUserId(testUserId);
        c2.setDeleted(false);
        categoryRepository.save(c2);

        Category c3_deleted = new Category();
        c3_deleted.setName("カテゴリ3");
        c3_deleted.setUserId(testUserId);
        c3_deleted.setDeleted(true); // 削除済みのカテゴリ
        categoryRepository.save(c3_deleted);

        Category c4_anotherUser = new Category();
        c4_anotherUser.setName("カテゴリ4");
        c4_anotherUser.setUserId(anotherUserId);  // 別のユーザー
        c4_anotherUser.setDeleted(false);
        categoryRepository.save(c4_anotherUser);

        // Act
        long count = categoryRepository.countByUserIdAndDeletedFalse(testUserId);

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void existsByNameAndUserIdInAndDeletedFalse_指定したユーザーリスト内に未削除のカテゴリが存在する場合_trueを返す() {
        // Arrange
        String targetCategoryName = "存在するカテゴリ";
        String targetUser = "user1";
        String anotherUser = "user2";
        String systemUser = "system";

        Category category = new Category();
        category.setName(targetCategoryName);
        category.setUserId(targetUser);
        category.setDeleted(false);
        categoryRepository.save(category);

        // Act & Assert
        // 存在するユーザーを含むリストで検索した場合
        List<String> userListContainingTarget = List.of(targetUser, systemUser);
        boolean result1 = categoryRepository.existsByNameAndUserIdInAndDeletedFalse(targetCategoryName, userListContainingTarget);
        assertThat(result1).isTrue();

        // 存在しないユーザーのリストで検索した場合
        List<String> userListNotContainingTarget = List.of(anotherUser, systemUser);
        boolean result2 = categoryRepository.existsByNameAndUserIdInAndDeletedFalse(targetCategoryName, userListNotContainingTarget);
        assertThat(result2).isFalse();

        // 違う名前で検索した場合
        boolean result3 = categoryRepository.existsByNameAndUserIdInAndDeletedFalse("存在しないカテゴリ", userListContainingTarget);
        assertThat(result3).isFalse();
    }

    @Test
    void existsByNameAndUserIdInAndDeletedFalse_カテゴリは存在するが削除済みの場合_falseを返す() {
        // Arrange
        String targetCategoryName = "削除済みカテゴリ";
        String targetUser = "user1";
        String systemUser = "system";

        Category category = new Category();
        category.setName(targetCategoryName);
        category.setUserId(targetUser);
        category.setDeleted(true); // 削除済みに設定
        categoryRepository.save(category);

        // Act
        List<String> userList = List.of(targetUser, systemUser);
        boolean result = categoryRepository.existsByNameAndUserIdInAndDeletedFalse(targetCategoryName, userList);

        // Assert
        assertThat(result).isFalse();
    }




    /**
     * GET /categories のテスト
     */
    @Test
    void findByUserIdAndDeletedFalseOrUserIdAndDeletedFalse_カスタムカテゴリとデフォルトカテゴリを併せて名前順に取得する() {
        // Arrange
        String testUserId = "user1";
        String testSystemUserId = "system";

        // テストデータの作成
        Category custumCategory = new Category();
        custumCategory.setName("カスタムカテゴリ");
        custumCategory.setUserId(testUserId);
        custumCategory.setDeleted(false);
        categoryRepository.save(custumCategory);

        Category defaultCategory = new Category();
        defaultCategory.setName("デフォルトカテゴリ");
        defaultCategory.setUserId(testSystemUserId); // システムユーザーが作成
        defaultCategory.setDeleted(false);
        categoryRepository.save(defaultCategory);

        Category deletedCategory = new Category();
        deletedCategory.setName("削除済みカテゴリ");
        deletedCategory.setUserId(testUserId);
        deletedCategory.setDeleted(true); // 削除済みのカテゴリ
        categoryRepository.save(deletedCategory);

        // Act
        List<Category> result = categoryRepository.findByUserIdAndDeletedFalseOrUserIdAndDeletedFalse(testUserId, testSystemUserId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("カスタムカテゴリ"); // 「カ」が「デ」より先に表示される
        assertThat(result.get(1).getName()).isEqualTo("デフォルトカテゴリ");
    }
}
