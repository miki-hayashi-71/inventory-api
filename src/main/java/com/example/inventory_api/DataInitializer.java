package com.example.inventory_api;

import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.model.Item;
import com.example.inventory_api.domain.repository.CategoryRepository;
import com.example.inventory_api.domain.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// アプリの起動時に自動で呼び出される
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner{

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    @Override
    public void run(String... args) throws Exception {
        // デフォルトカテゴリのデータ作成

        // システムユーザー（仮）
        final String SYSTEM_USER_ID = "system";

        // 作成するデフォルトカテゴリのリスト
        List<String> categoryNames = List.of(
                "キッチン",
                "バスルーム",
                "洗面所・脱衣所",
                "トイレ",
                "リビング・ダイニング",
                "寝室",
                "玄関",
                "収納・クローゼット",
                "掃除用品",
                "備蓄・防災"
        );

        for (String name : categoryNames) {
            // アプリを起動するたびにデフォルトカテゴリが作成されないよう、重複チェックを行う
            if (categoryRepository.findByNameAndUserIdAndDeletedFalse(name, SYSTEM_USER_ID).isEmpty()) {
                Category category = new Category();
                category.setUserId(SYSTEM_USER_ID);
                category.setName(name);
                category.setDeleted(false);
                categoryRepository.save(category);
            }
        }

        // TODO: postItemの実装時に削除する（deleteCategorymの実装確認用のため）
        final String TENTATIVE_CATEGORY_NAME = "削除確認用カテゴリ";
        final String TENTATIVE_USER_ID = "user1"; // Controllerの仮ユーザーIDと合わせる

        if (categoryRepository.findByNameAndUserIdAndDeletedFalse(TENTATIVE_CATEGORY_NAME, TENTATIVE_USER_ID).isEmpty()) {
            // カテゴリを作成
            Category testCategory = new Category(TENTATIVE_USER_ID, TENTATIVE_CATEGORY_NAME, false);
            Category savedCategory = categoryRepository.save(testCategory);

            // 上記のカテゴリに紐づくアイテムを作成
            Item testItem = new Item(savedCategory.getId(), TENTATIVE_USER_ID, "カテゴリに属するアイテム", 1, false);
            itemRepository.save(testItem);
        }
    }
}
