package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Integer> {

  /**
   * 指定されたカテゴリIDに紐づく、未削除のアイテムが存在するかどうかを判定します。
   *
   * @param categoryId カテゴリID
   * @return アイテムが存在する場合は true, 存在しない場合は false
   */
  @Query(value = """
      SELECT EXISTS (
          SELECT 1
          FROM items
          WHERE category_id = :categoryId
          AND deleted = false
      )
      """, nativeQuery = true)
  boolean existsItems(@Param("categoryId") Integer categoryId);

}
