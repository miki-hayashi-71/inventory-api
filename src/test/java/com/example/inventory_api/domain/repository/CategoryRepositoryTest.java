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
    void findByUerIdAndDeletedFalse_指定したユーザーIDリストに紐づく見削除カテゴリのみを返す() {
        // Arrange
        // 1. テストユーザーのカテゴリ (取得対象)
        Category c1 = new Category();
        c1.setName("カテゴリ1");
        c1.setUserId(testUserId);
        c1.setDeleted(false);
        categoryRepository.save(c1);

        // 2. システムユーザーのカテゴリ (取得対象)
        Category c2 = new Category();
        c2.setName("カテゴリ2");
        c2.setUserId(systemUserId);
        c2.setDeleted(false);
        categoryRepository.save(c2);

        // 3. テストユーザーの削除済みカテゴリ (取得対象外)
        Category c3_deleted = new Category();
        c3_deleted.setName("カテゴリ3");
        c3_deleted.setUserId(testUserId);
        c3_deleted.setDeleted(true);
        categoryRepository.save(c3_deleted);

        // 4. 別のユーザーのカテゴリ (取得対象外)
        Category c4_anotherUser = new Category();
        c4_anotherUser.setName("カテゴリ4");
        c4_anotherUser.setUserId(anotherUserId);
        c4_anotherUser.setDeleted(false);
        categoryRepository.save(c4_anotherUser);

        // Act
        List<String> userIdsToSearch = List.of(testUserId, systemUserId);
        List<Category> result = categoryRepository.findByUserIdInAndDeletedFalse(userIdsToSearch);

        // Assert
        assertThat(result).hasSize(2); // c1 と c2 の2件が取得されるはず
        assertThat(result).extracting(Category::getName).containsExactlyInAnyOrder("カテゴリ1", "カテゴリ2");
    }
}
