package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Item;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Integer> {

  /**
   * 指定されたカテゴリIDに紐づく、未削除のアイテムのリストを取得します。
   *
   * @param categoryId カテゴリID
   * @return 条件に一致するアイテムのリスト
   */
  @Query(value = """
      SELECT *
      FROM items
      WHERE category_id = :categoryId
      AND deleted = false
      """, nativeQuery = true)
  List<Item> undeletedItems(@Param("categoryId") Integer categoryId);
}
