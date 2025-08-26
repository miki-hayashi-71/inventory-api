package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Optional;

// Categoryエンティティとやり取りを行うリポジトリ（インターフェース）
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * 指定した名称とユーザーIDに一致し、かつ論理削除されていないカテゴリを検索
     * DataInitializerでの重複チェックに使用
     * @param name カテゴリ名
     * @param userId ユーザーID
     * @return 条件に一致するカテゴリのリスト
     */
    List<Category> findByNameAndUserIdAndDeletedFalse(String name, String userId);

    /**
     * 指定した複数のユーザーIDに一致し、かつ論理削除されていないカテゴリを全て取得
     * @param userIds ユーザーIDのリスト
     * @return 条件に一致するカテゴリのリスト
     */
    List<Category> findByUserIdInAndDeletedFalse(List<String> userIds);

    /**
     * ログインユーザーのカスタムカテゴリと、システムのデフォルトカテゴリの一覧を取得
     * @param userId ログインユーザーのID
     * @param systemUserId システムユーザーのID
     * @return ログインユーザーの未削除カテゴリと、システムの未削除カテゴリの合算リスト
     */
    List<Category> findByUserIdAndDeletedFalseOrUserIdAndDeletedFalse(String userId, String systemUserId);

    // 指定したnameとuserIdを持つ削除されていないカテゴリを検索
    Optional<Category> findByNameAndUserIdAndDeletedFalse(String name, String userId);

    // 指定したnameに一致するカテゴリを、ユーザーIDリストの中から検索
    List<Category> findByNameAndUserIdInAndDeletedFalse(String name, List<String> userIds);
}
