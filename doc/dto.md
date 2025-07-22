# DTO定義書
各 API エンドポイントのリクエストボディの構造とバリデーションを記述します。レスポンスは Swagger UI を参照してください。

## Category

### CategoryResponse
- カテゴリ情報をクライアントへ返すレスポンスのためのDTO

| フィールド名    | データ型          | 必須 | バリデーション | 説明     |
|-----------|---------------|----|---------|--------|
| id        | Integer       | ○  |         | カテゴリID |
| name      | String        | ○  |         | カテゴリ名  |
| createdAt | LocalDateTime | ○  |         | 作成日時   |
| updatedAt | LocalDateTime | ○  |         | 更新日時   |


### CategoryCreateRequest
- カテゴリを新規作成する際に、クライアントから受け取るリクエストボディのためのDTO

| フィールド名 | データ型   | 必須 | バリデーション                  | 説明    |
|--------|--------|----|--------------------------|-------|
| name   | String | ○  | @NotBlank, @Size(max=30) | カテゴリ名 |


### CategoryUpdateRequest
- 既存カテゴリを更新する際に、クライアントから受け取るリクエストボディのためのDTO

| フィールド名 | データ型   | 必須 | バリデーション                  | 説明       |
|--------|--------|----|--------------------------|----------|
| name   | String | ○  | @NotBlank, @Size(max=30) | 新しいカテゴリ名 |


## Item

### ItemResponse
- アイテム情報をクライアントへ返すレスポンスのためのDTO

| フィールド名     | データ型          | 必須 | バリデーション | 説明     |
|------------|---------------|----|---------|--------|
| id         | Integer       | ○  |         | アイテムID |
| name       | String        | ○  |         | アイテム名  |
| quantity   | Integer       | ○  |         | 在庫数    |
| categoryId | Integer       | ○  |         | カテゴリID |
| createdAt  | LocalDateTime | ○  |         | 作成日時   |
| updatedAt  | LocalDateTime | ○  |         | 更新日時   |


### ItemCreateRequest
- アイテムを新規作成する際に、クライアントから受け取るリクエストボディのためのDTO

| フィールド名     | データ型    | 必須 | バリデーション                  | 説明           |
|------------|---------|----|--------------------------|--------------|
| name       | String  | ○  | @NotBlank, @Size(max=50) | アイテム名        |
| quantity   | Integer |    | @PositiveOrZero          | 在庫数。未指定の場合は0 |
| categoryId | Integer | ○  | @NotNull                 | カテゴリID       |

### ItemUpdateRequest
- 既存アイテムを更新する際に、クライアントから受け取るリクエストボディのためのDTO

| フィールド名     | データ型    | 必須 | バリデーション                   | 説明       |
|------------|---------|--|---------------------------|----------|
| name       | String  | ○ | @NotBlank, @Size(max=50)  | 新しいアイテム名 |
| quantity   | Integer | ○ | @NotNull, @PositiveOrZero | 更新後の在庫数  |


## 補足

| アノテーション         | チェック内容        | 対象型                           |
|-----------------|---------------|-------------------------------|
| @NotNull        | null禁止        | なんでも                          |
| @NotBlank       | null・空文字・空白文字禁止 | String のみ                     |
| @PositiveOrZero | 0以上           | Integer, Long, Double         |
