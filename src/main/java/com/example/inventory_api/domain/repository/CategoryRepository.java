package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Categoryエンティティとやり取りを行うリポジトリ（インターフェース）
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // デフォルトカテゴリの重複作成を防ぐため、nameとuserIdで一致するものをチェックする。DataInitializerではここが空の場合に処理を進める
    List<Category> findByNameAndUserIdAndDeletedFalse(String name, String userId);

    // 指定したuserIdの削除されていないカテゴリを全て取得
    List<Category> findByUserIdInAndDeletedFalse(List<String> userIds);

    // ログインユーザーのカスタムカテゴリとデフォルトカテゴリの一覧を取得する
    // 「(user_idかつ削除されていない)または(2つめの引数のsystemUser_idかつ削除されていない)もの」を取得
    List<Category> findByUserIdAndDeletedFalseOrUserIdAndDeletedFalse(String userId, String systemUserId);
}
