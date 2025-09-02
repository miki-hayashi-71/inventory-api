package com.example.inventory_api.service;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.controller.dto.CategoryResponse;
import com.example.inventory_api.controller.dto.CategoryUpdateRequest;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.model.Item;
import com.example.inventory_api.domain.repository.CategoryRepository;
import com.example.inventory_api.domain.repository.ItemRepository;
import com.ibm.icu.text.Collator;
import com.ibm.icu.util.ULocale;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final ItemRepository itemRepository;
  private final MessageSource messageSource;

  // @Valueアノテーションでプロパティファイルから値を読み込む
  @Value("${app.custom-category.max-limit}")
  private int maxCustomCategoryLimit;

  // システムユーザー（仮）
  private static final String SYSTEM_USER_ID = "system";

  /**
   * 新しいカスタムカテゴリを1件登録 createCategory
   */
  @Transactional  // このメソッド内の処理をすべて一つのトランザクション（全て成功or全て失敗）として実行
  public Category createCategory(
      CategoryCreateRequest request,
      String userId
  ) {
    try {
      List<Category> existingCategories = categoryRepository.findUserCategories(userId,
          SYSTEM_USER_ID);

      // 重複チェック
      validateCategoryName(request.getName(), null, existingCategories);

      // 上限チェック
      long userCategoryCount = existingCategories.stream()
          .filter(category -> userId.equals(category.getUserId()))
          .count();
      if (userCategoryCount >= maxCustomCategoryLimit) {
        throw new IllegalStateException(
            getMessage("prefix.limit") + getMessage("category.limit")
        );
      }

      // 新しいカテゴリを作成して保存
      Category newCategory = new Category(userId, request.getName(), false);
      return categoryRepository.save(newCategory);

    } catch (IllegalStateException e) {
      throw e;
    } catch (DataAccessException e) {
      throw new RuntimeException(getMessage("db.accessError"), e);
    } catch (NullPointerException | IllegalArgumentException e) {  // TODO: 例外パターンを追加する
      throw new RuntimeException(getMessage("error.unexpected"), e);
    } catch (Exception e) {
      throw new RuntimeException(getMessage("error.unexpected"), e);
    }
  }

  /**
   * カスタムカテゴリの一覧を取得 getCategoryList
   */
  public List<CategoryResponse> getCategoryList(String userId) {
    try {
      List<Category> categories = categoryRepository.findUserCategories(userId, SYSTEM_USER_ID);

      // 日本語の辞書順でソートするためのCollatorを準備
      Collator collator = Collator.getInstance(ULocale.JAPANESE);

      // ソートしてレスポンスに変換する
      return categories.stream()
          .sorted(Comparator.comparing(Category::getName, collator))
          .map(CategoryResponse::new)
          .collect(Collectors.toList());

    } catch (DataAccessException e) {
      throw new RuntimeException(getMessage("db.accessError"), e);
    } catch (Exception e) {
      throw new RuntimeException(getMessage("error.unexpected"), e);
    }
  }

  /**
   * カスタムカテゴリを1件更新 updateCategory
   */
  @Transactional
  public Category updateCategory(
      Integer categoryId,
      CategoryUpdateRequest request,
      String userId
  ) {
    try {
      List<Category> categories = categoryRepository.findUserCategories(userId, SYSTEM_USER_ID);

      // 重複チェック
      validateCategoryName(request.getName(), categoryId, categories);

      // 更新対象の検索
      Category categoryToUpdate = categories.stream()
          .filter(category -> category.getId().equals(categoryId))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException(
              getMessage("prefix.notFound") + getMessage("category.notFound")
          ));

      // 権限チェック
      validateCategoryPermission(categoryToUpdate, userId);

      categoryToUpdate.setName(request.getName());
      return categoryRepository.save(categoryToUpdate);

    } catch (IllegalStateException e) {
      throw e;
    } catch (DataAccessException e) {
      throw new RuntimeException(getMessage("db.accessError"), e);
    } catch (NullPointerException e) {  // TODO: 例外パターンを追加する
      throw new RuntimeException(getMessage("error.unexpected"), e);
    } catch (Exception e) {
      throw new RuntimeException(getMessage("error.unexpected"), e);
    }
  }

  /**
   * 指定されたIDのカスタムカテゴリを1件論理削除 DELETE /categories/{categoryId}
   */
  @Transactional
  public void deleteCategory(
      Integer categoryId,
      String userId
  ) {
    try {
      List<Category> categories = categoryRepository.findUserCategories(userId, SYSTEM_USER_ID);

      // 削除対象の検索
      Category categoryToDelete = categories.stream()
          .filter(category -> category.getId().equals(categoryId))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException(
              getMessage("prefix.notFound") + getMessage("category.notFound")
          ));

      // 権限チェック
      validateCategoryPermission(categoryToDelete, userId);

      // カテゴリに紐づく未削除のアイテムを取得
      List<Item> items = itemRepository.findExistingItems(categoryId);

      // アイテムが存在する場合、エラーメッセージにアイテム名を追記して例外をスロー
      if (!items.isEmpty()) {
        String itemNames = items.stream()
            .map(Item::getName)
            .collect(Collectors.joining(", "));
        // 定数を組み合わせてエラーメッセージを生成
        String errorMessage = getMessage("category.notEmpty")
                              + getMessage("category.existingItems", itemNames);
        throw new IllegalStateException(getMessage("prefix.conflict") + errorMessage);
      }

      // 論理削除
      categoryToDelete.setDeleted(true);
      categoryRepository.save(categoryToDelete);

    } catch (IllegalStateException e) {
      throw e;
    } catch (DataAccessException e) {
      throw new RuntimeException(getMessage("db.accessError"), e);
    } catch (NullPointerException e) {  // TODO: 例外パターンを追加する
      throw new RuntimeException(getMessage("error.unexpected"), e);
    } catch (Exception e) {
      throw new RuntimeException(getMessage("error.unexpected"), e);
    }
  }

  /**
   * カテゴリ名の重複をチェックする共通メソッド
   *
   * @param newName             新しいカテゴリ名
   * @param categoryIdToExclude 重複チェックから除外するカテゴリID (更新時に使用)
   * @param categories          チェック対象のカテゴリリスト
   */
  private void validateCategoryName(
      String newName,
      Integer categoryIdToExclude,
      List<Category> categories
  ) {
    boolean isDuplicate = categories.stream()
        .filter(category -> !category.getId().equals(categoryIdToExclude))
        .anyMatch(category -> category.getName().equals(newName));

    if (isDuplicate) {
      throw new IllegalStateException(
          getMessage("prefix.duplicate") + getMessage("category.duplicate")
      );
    }
  }

  /**
   * カテゴリの操作権限を検証する
   *
   * @param category 操作対象のカテゴリ
   * @param userId   操作を実行しようとしているユーザーID
   */
  private void validateCategoryPermission(Category category, String userId) {
    // デフォルトカテゴリは操作不可
    if (SYSTEM_USER_ID.equals(category.getUserId())) {
      throw new IllegalStateException(
          getMessage("prefix.forbidden") + getMessage("category.forbidden.default")
      );
    }
    // 他人のカテゴリは操作不可
    if (!userId.equals(category.getUserId())) {
      throw new IllegalStateException(
          getMessage("prefix.forbidden") + getMessage("category.forbidden")
      );
    }
  }

  /**
   * messages.propertiesからメッセージを取得する (引数なし)
   */
  private String getMessage(String code) {
    return messageSource.getMessage(code, null, Locale.JAPAN);
  }

  /**
   * messages.propertiesからメッセージを取得する (引数あり)
   */
  private String getMessage(String code, Object... args) {
    return messageSource.getMessage(code, args, Locale.JAPAN);
  }
}
