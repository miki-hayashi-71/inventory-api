package com.example.inventory_api.service;

import com.example.inventory_api.controller.dto.CategoryCreateRequest;
import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.repository.CategoryRepository;
import com.example.inventory_api.service.exception.CategoryLimitExceededException;
import com.example.inventory_api.service.exception.CategoryNameDuplicateException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // システムユーザー（仮）
    private static final String SYSTEM_USER_ID = "system";

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * 新しいカスタムカテゴリを1件登録
     * POST /categories
     */
    @Transactional  // このメソッド内の処理をすべて一つのトランザクション（全て成功or全て失敗）として実行
    public Category createCategory(CategoryCreateRequest request, String userId) {

        // 要件：デフォルトカテゴリ及び自身のカスタムカテゴリ内で、カテゴリ名が重複しない
        categoryRepository.findByNameAndUserIdAndDeletedFalse(request.getName(), userId)
                .ifPresent(c -> {
                    // カスタムカテゴリに同じカテゴリ名が存在すれば例外をスロー
                    throw new CategoryNameDuplicateException("そのカテゴリ名は既に使用されています");
                });
        categoryRepository.findByNameAndUserIdAndDeletedFalse(request.getName(), SYSTEM_USER_ID)
                .ifPresent(c -> {
                    // デフォルトカテゴリに同じカテゴリ名が存在すれば例外をスロー
                    throw new CategoryNameDuplicateException("そのカテゴリ名は既に使用されています");
                });

        // 要件：ユーザーが作成できるカスタムカテゴリは50件まで
        long categoryCount = categoryRepository.countByUserIdAndDeletedFalse(userId);
        if (categoryCount >= 50) {
            throw new CategoryLimitExceededException("登録できるカテゴリの上限に達しています");
        }

        // 新しいカテゴリを作成して保存
        Category newCategory = new Category();

        newCategory.setName(request.getName());
        newCategory.setUserId(userId);
        newCategory.setDeleted(false);

        return categoryRepository.save(newCategory);
    }

    /**
      カスタムカテゴリの一覧を取得
      GET /categories
      */
    public List<Category> getCategoryList(String userId) {
        return categoryRepository.findByUserIdAndDeletedFalseOrUserIdAndDeletedFalseOrderByNameAsc(userId, SYSTEM_USER_ID);
    }
}
