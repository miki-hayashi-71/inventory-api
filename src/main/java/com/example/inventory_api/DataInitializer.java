package com.example.inventory_api;

import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.model.Item;
import com.example.inventory_api.domain.repository.CategoryRepository;
import com.example.inventory_api.domain.repository.ItemRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// アプリの起動時に自動で呼び出される
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final CategoryRepository categoryRepository;
  private final ItemRepository itemRepository;

  @Override
  public void run(String... args) throws Exception {
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
      if (categoryRepository.findExistingCategories(name, SYSTEM_USER_ID).isEmpty()) {
        Category category = new Category(SYSTEM_USER_ID, name, false);
        categoryRepository.save(category);
      }
    }

    // TODO: postItemの実装時に削除する（deleteCategoryの実装確認用のため）
    final String TENTATIVE_CATEGORY_NAME = "削除確認用カテゴリ";
    final String TENTATIVE_USER_ID = "user1";

    Optional<Category> existingCategory = categoryRepository.findExistingCategories(
        TENTATIVE_CATEGORY_NAME,
        TENTATIVE_USER_ID
    );

    if (existingCategory.isEmpty()) {
      Category testCategory = new Category(
          TENTATIVE_USER_ID,
          TENTATIVE_CATEGORY_NAME,
          false
      );

      Category savedCategory = categoryRepository.save(testCategory);

      // 上記のカテゴリに紐づくアイテムを作成
      List<String> itemNames = List.of(
          "アイテム1",
          "アイテム2",
          "アイテム3"
      );

      for (String itemName : itemNames) {
        Item testItem = new Item(
            savedCategory.getId(),
            TENTATIVE_USER_ID,
            itemName,
            1,
            false
        );
        itemRepository.save(testItem);
      }
    }
  }
}
