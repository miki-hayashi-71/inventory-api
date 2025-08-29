package com.example.inventory_api.service;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.controller.dto.CategoryResponse;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.repository.CategoryRepository;
import com.example.inventory_api.domain.repository.ItemRepository;
import com.ibm.icu.text.Collator;
import com.ibm.icu.util.ULocale;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    // @Valueアノテーションでプロパティファイルから値を読み込む
    @Value("${app.custom-category.max-limit}")
    private int maxCustomCategoryLimit;

    // システムユーザー（仮）
    private static final String SYSTEM_USER_ID = "system";

    // エラーメッセージを定数で管理
    // TODO: メッセージ用のプロパティファイルを用意したい（https://github.com/miki-hayashi-71/inventory-api/pull/18#discussion_r2299559046）
    private static final String VALID_DUPLICATE_MESSAGE = "DUPLICATE:そのカテゴリ名は既に使用されています";
    private static final String VALID_LIMIT_MESSAGE = "LIMIT:登録できるカテゴリの上限に達しています";
    private static final String DATABASE_ACCESS_FAILURE_MESSAGE = "データベースへの保存に失敗しました";
    private static final String UNEXPECTED_ERROR_MESSAGE = "予期せぬエラーが発生しました";

    // deleteCategory用
    private static final String MSG_NOT_FOUND_DELETE = "NOT_FOUND:該当のカテゴリが見つかりません";
    private static final String MSG_FORBIDDEN_DELETE = "FORBIDDEN:このカテゴリを操作する権限がありません";
    private static final String MSG_FORBIDDEN_DEFAULT_DELETE = "FORBIDDEN:デフォルトカテゴリは削除できません";
    private static final String MSG_CONFLICT_DELETE = "CONFLICT:アイテムが1件以上登録されているカテゴリは削除できません";

    // 共通
    private static final String MSG_DB_ACCESS_ERROR = "データベースへの保存に失敗しました";
    private static final String MSG_UNEXPECTED_ERROR = "予期せぬエラーが発生しました";

    /**
     * 新しいカスタムカテゴリを1件登録
     * POST /categories
     */
    @Transactional  // このメソッド内の処理をすべて一つのトランザクション（全て成功or全て失敗）として実行
    public Category createCategory(CategoryCreateRequest request, String userId) {

        try {
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

            return categoryRepository.save(newCategory);
        } catch (IllegalStateException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new RuntimeException(DATABASE_ACCESS_FAILURE_MESSAGE, e);
        } catch (NullPointerException | IllegalArgumentException e) {  // TODO: 例外パターンを追加する
            throw new RuntimeException(UNEXPECTED_ERROR_MESSAGE, e);
        } catch (Exception e) {
            throw new RuntimeException(UNEXPECTED_ERROR_MESSAGE, e);
        }
    }

    /**
     * カスタムカテゴリの一覧を取得
     * GET /categories
     */
    public List<CategoryResponse> getCategoryList(String userId) {
        try {
            // DBからカスタムカテゴリとデフォルトカテゴリを取得する
            List<Category> categories = categoryRepository.findUserCategories(userId, SYSTEM_USER_ID);

            // 日本語の辞書順でソートするためのCollatorを準備
            Collator collator = Collator.getInstance(ULocale.JAPANESE);

            // ソートしてレスポンスに変換する
            return categories.stream()
                    .sorted(Comparator.comparing(Category::getName, collator))
                    .map(category -> new CategoryResponse(category.getId(), category.getName()))
                    .collect(Collectors.toList());

        } catch (DataAccessException e) {
            throw new RuntimeException(DATABASE_ACCESS_FAILURE_MESSAGE, e);
        } catch (Exception e) {
            throw new RuntimeException(UNEXPECTED_ERROR_MESSAGE, e);
        }
    }

    /**
     * 指定されたIDのカスタムカテゴリを1件論理削除
     * DELETE /categories/{categoryId}
     */
    @Transactional
    public void deleteCategory(Integer categoryId, String userId) {
        try {
            // IDでカテゴリを直接検索
            Category categoryToDelete = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalStateException(MSG_NOT_FOUND_DELETE));

            // 削除済みでないか
            if (categoryToDelete.getDeleted()) {
                throw new IllegalStateException(MSG_NOT_FOUND_DELETE);
            }

            // 権限チェック
            if (!categoryToDelete.getUserId().equals(userId)) {
                // デフォルトカテゴリの場合
                if (SYSTEM_USER_ID.equals(categoryToDelete.getUserId())) {
                    throw new IllegalStateException(MSG_FORBIDDEN_DEFAULT_DELETE);
                }
                // 他人のカテゴリの場合
                throw new IllegalStateException(MSG_FORBIDDEN_DELETE);
            }

            // カテゴリに紐づくアイテムの有無
            if (itemRepository.existsItems(categoryId)) {
                throw new IllegalStateException(MSG_CONFLICT_DELETE);
            }

            // 論理削除
            categoryToDelete.setDeleted(true);
            categoryRepository.save(categoryToDelete);

        } catch (IllegalStateException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new RuntimeException(MSG_DB_ACCESS_ERROR, e);
        } catch (Exception e) {
            throw new RuntimeException(MSG_UNEXPECTED_ERROR, e);
        }
    }
}
