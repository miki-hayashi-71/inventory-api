package com.example.inventory_api.service;

import com.example.inventory_api.controller.dto.ItemCreateRequest;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.model.Item;
import com.example.inventory_api.domain.repository.CategoryRepository;
import com.example.inventory_api.domain.repository.ItemRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;
  private final CategoryRepository categoryRepository;

  private static final String SYSTEM_USER_ID = "system";

  // エラーメッセージ
  private static final String MSG_CATEGORY_NOT_FOUND = "NOT_FOUND:指定されたカテゴリは見つかりません";
  private static final String MSG_DUPLICATE = "CONFLICT:そのアイテム名は既に登録されています";
  private static final String MSG_DB_ACCESS_ERROR = "データベースへのアクセスに失敗しました";
  private static final String MSG_UNEXPECTED_ERROR = "予期せぬエラーが発生しました";

  /**
   * 新しいアイテムを1件登録 createItem
   */
  @Transactional
  public Item createItem(
      ItemCreateRequest request,
      String userId
  ) {
    try {
      // 指定したIDでカテゴリを取得
      Category category = categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new IllegalStateException(MSG_CATEGORY_NOT_FOUND));

      // カテゴリの権限チェック
      if (!category.getUserId().equals(userId) && !category.getUserId().equals(SYSTEM_USER_ID)) {
        throw new IllegalStateException(MSG_CATEGORY_NOT_FOUND);
      }

      // カテゴリ内の既存アイテムを全て取得
      List<Item> existingItems = itemRepository.findUserCategoryItems(
          request.getCategoryId(),
          userId
      );

      // アイテム名の重複チェック
      boolean isDuplicate = existingItems.stream()
          .anyMatch(item -> item.getName().equals(request.getName()));
      if (isDuplicate) {
        throw new IllegalStateException(MSG_DUPLICATE);
      }

      Item newItem = new Item(request, userId);
      return itemRepository.save(newItem);

    } catch (IllegalStateException e) {
      throw e;
    } catch (DataAccessException e) {
      throw new RuntimeException(MSG_DB_ACCESS_ERROR, e);
    } catch (Exception e) {
      throw new RuntimeException(MSG_UNEXPECTED_ERROR, e);
    }
  }
}
