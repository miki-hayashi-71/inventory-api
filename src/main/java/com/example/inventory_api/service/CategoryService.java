package com.example.inventory_api.service;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.controller.dto.CategoryUpdateRequest;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.Collator;
import com.ibm.icu.util.ULocale;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // システムユーザー（仮）
    private static final String SYSTEM_USER_ID = "system";

    /**
     * 新しいカスタムカテゴリを1件登録
     * POST /categories
     */
    @Transactional  // このメソッド内の処理をすべて一つのトランザクション（全て成功or全て失敗）として実行
    public Category createCategory(CategoryCreateRequest request, String userId) {

        // ログインユーザーとシステムユーザーのカテゴリを取得する
        List<String> userIdsToCheck = List.of(userId, SYSTEM_USER_ID);
        List<Category> existingCategories = categoryRepository.findByUserIdInAndDeletedFalse(userIdsToCheck);

        // 重複チェック
        boolean isDuplicate = existingCategories.stream()
                .anyMatch(category -> category.getName().equals(request.getName()));
        if (isDuplicate) {
            throw new IllegalStateException("DUPLICATE:そのカテゴリ名は既に使用されています");
        }

        // 取得したリストからカスタムカテゴリの上限チェック
        long userCategoryCount = existingCategories.stream()
                .filter(category -> category.getUserId().equals(userId))
                .count();
        if (userCategoryCount >= 50) {
            throw new IllegalStateException("LIMIT:登録できるカテゴリの上限に達しています");
        }

        // 新しいカテゴリを作成して保存
        Category newCategory = new Category();

        newCategory.setName(request.getName());
        newCategory.setUserId(userId);
        newCategory.setDeleted(false);

        try {
            return categoryRepository.save(newCategory);
        } catch (DataAccessException e) {
            throw new RuntimeException("データベースへの保存に失敗しました", e);
        }
    }

    /**
      カスタムカテゴリの一覧を取得
      GET /categories
      */
    public List<Category> getCategoryList(String userId) {
        // ログインユーザーとシステムユーザーのIDをリストにまとめる
        List<String> userIdsToSearch = List.of(userId, SYSTEM_USER_ID);
        // 共通化されたメソッドを呼び出す
        List<Category> categories = categoryRepository.findByUserIdInAndDeletedFalse(userIdsToSearch);

        // 日本語の辞書順でソート
        Collator collator = Collator.getInstance(ULocale.JAPANESE);
        collator.setStrength(Collator.PRIMARY);
        categories.sort(Comparator.comparing(Category::getName, collator));

        return categories;
    }

    /**
     * カスタムカテゴリを1件更新
     * PATCH /categories/{categoryId}
     */
    @Transactional
    public Category updateCategory(Integer categoryId, CategoryUpdateRequest request, String userId) {
        // 更新対象のカテゴリをDBから取得
        Category categoryToUpdate = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("NOT_FOUND:該当のカテゴリが見つかりません"));

        // 編集権限(デフォルトカテゴリでないか）をチェック
        if (SYSTEM_USER_ID.equals(categoryToUpdate.getUserId())) {
            throw new IllegalStateException("FORBIDDEN:デフォルトカテゴリは編集できません");
        }
        // 編集権限（自分以外のカテゴリでないか）をチェック
        if (!categoryToUpdate.getUserId().equals(userId)) {
            throw new IllegalStateException("FORBIDDEN:このカテゴリを編集する権限がありません");
        }

        // カテゴリ名の重複チェック
        List<String> userIdsToCheck = List.of(userId, SYSTEM_USER_ID);
        List<Category> duplicates = categoryRepository.findByNameAndUserIdInAndDeletedFalse(request.getName(), userIdsToCheck);

        // 取得したリストの中に、更新したいカテゴリ以外のカテゴリが含まれていればエラー
        if (duplicates.stream().anyMatch(c -> !c.getId().equals(categoryId))) {
            throw new IllegalStateException("DUPLICATE:そのカテゴリ名は既に使用されています");
        }

        // カテゴリ名を更新
        categoryToUpdate.setName(request.getName());

        try {
            return categoryRepository.save(categoryToUpdate);
        } catch (DataAccessException e) {
            throw new RuntimeException("データベースへの保存に失敗しました", e);
        }
    }
}
