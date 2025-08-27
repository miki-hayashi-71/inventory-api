package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Categoryエンティティとやり取りを行うリポジトリ（インターフェース）
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // デフォルトカテゴリの重複作成を防ぐため、nameとuserIdで一致するものをチェックする。DataInitializerではここが空の場合に処理を進める
    List<Category> findByNameAndUserIdAndDeletedFalse(String name, String userId);

    // 指定したuserIdの削除されていないカテゴリを全て取得
    List<Category> findByUserIdInAndDeletedFalse(List<String> userIds);

    // 指定したnameとuserIdを持つ削除されていないカテゴリを検索
    Optional<Category> findByNameAndUserIdAndDeletedFalse(String name, String userId);

    // 指定したnameに一致するカテゴリを、ユーザーIDリストの中から検索
    List<Category> findByNameAndUserIdInAndDeletedFalse(String name, List<String> userIds);
}
