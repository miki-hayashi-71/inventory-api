package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Item;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Integer> {

  /**
   * 指定したカテゴリIDとユーザーIDに紐づく、未削除のアイテムをすべて取得
   *
   * @param categoryId 検索対象のカテゴリID
   * @param userId     検索対象のユーザーID
   * @return 条件に一致する未削除のアイテムのリスト
   */
  @Query(value = """
      SELECT *
      FROM items
      WHERE category_id = :categoryId
      AND user_id = :userId
      AND deleted = false
      """, nativeQuery = true)
  List<Item> findUserCategoryItems(
      @Param("categoryId") Integer categoryId,
      @Param("userId") String userId
  );
}
