package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

// Categoryエンティティとやり取りを行うリポジトリ（インターフェース）
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // デフォルトカテゴリの重複作成を防ぐため、nameとuserIdで一致するものをチェックする。DataInitializerではここが空の場合に処理を進める
    List<Category> findByNameAndUserId(String name, String systemUserId);

    // カテゴリ名の重複チェックのため
    //「nameとuser_idが一致するものかつ削除されていないもの」を取得
    Optional<Category> findByNameAndUserIdAndDeletedFalse(String name, String userId);

    // ユーザーが作成できるカテゴリの上限数をチェックする
    // 「特定のuser_idかつ削除されていないもの」を数える
    long countByUserIdAndDeletedFalse(String userId);

    // ログインユーザーのカスタムカテゴリとデフォルトカテゴリの一覧を取得する
    // 「(user_idかつ削除されていない)または(2つめの引数のsystemUser_idかつ削除されていない)もの」を取得
    List<Category> findByUserIdAndDeletedFalseOrUserIdAndDeletedFalse(String userId, String systemUserId);
}
