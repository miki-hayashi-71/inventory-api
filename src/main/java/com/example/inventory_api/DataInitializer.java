package com.example.inventory_api;

import com.example.inventory_api.domain.model.Category;
import com.example.inventory_api.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// アプリの起動時に自動で呼び出される
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner{

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // デフォルトカテゴリのデータ作成

        // システムユーザー（仮）
        final String SYSTEM_USER_ID = "system";

        // 作成するデフォルトカテゴリのリスト
        List<String> categoryNames = List.of("キッチン", "バスルーム", "洗面所・脱衣所", "トイレ", "リビング・ダイニング", "寝室", "玄関", "収納・クローゼット", "掃除用品", "備蓄・防災");

        for (String name : categoryNames) {
            // アプリを起動するたびにデフォルトカテゴリが作成されないよう、重複チェックを行う
            if (categoryRepository.findByNameAndUserId(name, SYSTEM_USER_ID).isEmpty()) {
                Category category = new Category();
                category.setUserId(SYSTEM_USER_ID);
                category.setName(name);
                category.setDeleted(false);
                categoryRepository.save(category);
            }
        }
    }
}
