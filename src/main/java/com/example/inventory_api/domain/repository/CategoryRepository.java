package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Categoryエンティティとやり取りを行うリポジトリ（インターフェース）
public interface CategoryRepository extends JpaRepository<Category, Integer> {

  /**
   * 指定した名称とユーザーIDに一致し、かつ論理削除されていないカテゴリを検索 DataInitializerでの重複チェックに使用
   *
   * @param name   カテゴリ名
   * @param userId ユーザーID
   * @return 条件に一致するカテゴリのリスト
   */
  @Query(value = """
      SELECT *
      FROM categories
      WHERE name = :name
      AND user_id = :userId
      AND deleted = false
      """, nativeQuery = true)
  Optional<Category> findExistingCategories(
      @Param("name") String name,
      @Param("userId") String userId
  );

  /**
   * ログインユーザーのカスタムカテゴリと、システムのデフォルトカテゴリの一覧を取得
   *
   * @param userId       ログインユーザーのID
   * @param systemUserId システムユーザーのID
   * @return ログインユーザーの未削除カテゴリと、システムの未削除カテゴリの合算リスト
   */
  @Query(value = """
      SELECT *
      FROM categories
      WHERE user_id IN (:userId, :systemUserId)
      AND deleted = false
      """, nativeQuery = true)
  List<Category> findUserCategories(
      @Param("userId") String userId,
      @Param("systemUserId") String systemUserId
  );
}
