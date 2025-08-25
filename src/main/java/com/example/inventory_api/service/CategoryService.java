package com.example.inventory_api.service;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.Collator;
import com.ibm.icu.util.ULocale;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // @Valueアノテーションでプロパティファイルから値を読み込む
    @Value("${app.custom-category.max-limit}")
    private int maxCustomCategoryLimit;

    // システムユーザー（仮）
    private static final String SYSTEM_USER_ID = "system";

    // エラーメッセージを定数で管理
    private static final String VALID_DUPLICATE_MESSAGE = "DUPLICATE:そのカテゴリ名は既に使用されています";
    private static final String VALID_LIMIT_MESSAGE = "LIMIT:登録できるカテゴリの上限に達しています";
    private static final String DATABASE_SAVE_FAILURE_MESSAGE = "データベースへの保存に失敗しました";

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
            throw new IllegalStateException(VALID_DUPLICATE_MESSAGE);
        }

        // 取得したリストからカスタムカテゴリの上限チェック
        long userCategoryCount = existingCategories.stream()
                .filter(category -> category.getUserId().equals(userId))
                .count();
        if (userCategoryCount >= maxCustomCategoryLimit) {
            throw new IllegalStateException(VALID_LIMIT_MESSAGE);
        }

        // 新しいカテゴリを作成して保存
        Category newCategory = new Category();

        newCategory.setName(request.getName());
        newCategory.setUserId(userId);
        newCategory.setDeleted(false);

        try {
            return categoryRepository.save(newCategory);
        } catch (DataAccessException e) {
            throw new RuntimeException(DATABASE_SAVE_FAILURE_MESSAGE, e);
        }
    }

    /**
      カスタムカテゴリの一覧を取得
      GET /categories
      */
    public List<Category> getCategoryList(String userId) {
        // DBからカスタムカテゴリとデフォルトカテゴリを取得する
        List<Category> categories = categoryRepository.findByUserIdAndDeletedFalseOrUserIdAndDeletedFalse(userId, SYSTEM_USER_ID);

        // 日本語の辞書順でソートするためのCollatorを準備
        Collator collator = Collator.getInstance(ULocale.JAPANESE);

        // 取得したリストをCollatorで並び替え
        categories.sort(Comparator.comparing(Category::getName, collator));

        return categories;
    }
}
