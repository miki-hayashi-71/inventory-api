package com.example.inventory_api.domain.repository;

import com.example.inventory_api.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

// Categoryエンティティとやり取りを行うリポジトリ（インターフェース）
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // デフォルトカテゴリの重複作成を防ぐため、nameとuserIdで一致するものをチェックする。DataInitializerではここが空の場合に処理を進める
    List<Category> findByNameAndUserId(String name, String userId);

    // カテゴリ名の重複チェックのため
    Optional<Category> findByNameAndUserIdAndDeletedFalse(String name, String userId);

    // ユーザーが作成できるカテゴリの上限数をチェックする
    long countByUserIdAndDeletedFalse(String userId);
}
