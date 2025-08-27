package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Categoryエンティティとやり取りを行うリポジトリ（インターフェース）
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * 指定した名称とユーザーIDに一致し、かつ論理削除されていないカテゴリを検索
     * DataInitializerでの重複チェックに使用
     * @param name   カテゴリ名
     * @param userId ユーザーID
     * @return 条件に一致するカテゴリのリスト
     */
    // TODO: SQLでの取得に変更したい
    List<Category> findByNameAndUserIdAndDeletedFalse(String name, String userId);

    /**
     * 指定した複数のユーザーIDに一致し、かつ論理削除されていないカテゴリを全て取得
     * @param userIds ユーザーIDのリスト
     * @return 条件に一致するカテゴリのリスト
     */
    // TODO: createで使ってるけど、やってることはgetやupdateと一緒なので、これを削除してfindUserCategoriesに一本化できる
    List<Category> findByUserIdInAndDeletedFalse(List<String> userIds);

    /**
     * ログインユーザーのカスタムカテゴリと、システムのデフォルトカテゴリの一覧を取得
     * getCategoryList, updateCategoryで使用
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
    List<Category> findUserCategories(@Param("userId") String userId, @Param("systemUserId") String systemUserId);
}
