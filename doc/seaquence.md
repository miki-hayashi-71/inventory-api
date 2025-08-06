# シーケンス図
各処理のシーケンス図を記述します。

## ユーザー管理
### ユーザーのログイン認証（初回ログイン時のみ）
```mermaid
sequenceDiagram
    participant フロントエンド
    participant バックエンド
    participant Firebase Auth
    participant データベース

    フロントエンド->>Firebase Auth: ログイン要求 (初回)
    Firebase Auth-->>フロントエンド: IDトークン発行

    フロントエンド->>バックエンド: IDトークンを送信
    activate バックエンド
    バックエンド->>Firebase Auth: IDトークンを検証
    Firebase Auth-->>バックエンド: 検証OK (UIDを返却)

    バックエンド->>データベース: UIDをキーにユーザー存在確認
    activate データベース
    データベース-->>バックエンド: ユーザー存在有無
    deactivate データベース

    alt ユーザーがDBに存在しない場合
        バックエンド->>データベース: UIDをキーにユーザー情報をDBに保存
        activate データベース
        データベース-->>バックエンド: 保存完了
        deactivate データベース
    end

    バックエンド-->>フロントエンド: 200 OK
    deactivate バックエンド
```

## カテゴリ管理
### カテゴリ一覧取得
```mermaid
sequenceDiagram
    participant フロントエンド
    participant バックエンド
    participant Firebase Auth
    participant データベース

    フロントエンド->>バックエンド: GET /categories <br> (IDトークン)
    activate バックエンド
    バックエンド->>Firebase Auth: IDトークン検証
    Firebase Auth-->>バックエンド: 検証結果 (UID)

    par
        バックエンド->>データベース: デフォルトカテゴリ取得
        activate データベース
    and
        バックエンド->>データベース: カスタムカテゴリ取得 (UID)
    end

    データベース-->>バックエンド: カテゴリ一覧データ
    deactivate データベース

    バックエンド-->>フロントエンド: 200 OK (カテゴリ一覧)
    deactivate バックエンド
```


### カテゴリ新規作成
```mermaid
sequenceDiagram
    participant フロントエンド
    participant バックエンド
    participant Firebase Auth
    participant データベース

    フロントエンド->>バックエンド: POST /categories <br> (IDトークン, {name})
    activate バックエンド
    バックエンド->>Firebase Auth: IDトークン検証
    Firebase Auth-->>バックエンド: 検証結果 (UID)

    note over バックエンド: バリデーション (必須,文字数,上限数,重複)

    alt バリデーション失敗
        バックエンド-->>フロントエンド: 400 or 409 エラーレスポンス
    else バリデーション成功
        バックエンド->>データベース: 新規カテゴリ登録 (name, UID)
        activate データベース
        データベース-->>バックエンド: 登録したカテゴリ情報
        deactivate データベース
        バックエンド-->>フロントエンド: 201 Created (カテゴリ情報)
    end
    deactivate バックエンド
```

### カテゴリ編集
```mermaid
sequenceDiagram
    participant フロントエンド
    participant バックエンド
    participant Firebase Auth
    participant データベース

    フロントエンド->>バックエンド: PATCH /categories/{id} <br> (IDトークン, {name})
    activate バックエンド
    バックエンド->>Firebase Auth: IDトークン検証
    Firebase Auth-->>バックエンド: 検証結果 (UID)

    note over バックエンド: ① 権限・存在チェック <br> ② バリデーション (必須,文字数,重複)

    alt チェック失敗
        バックエンド-->>フロントエンド: 400, 403, 404, 409 エラーレスポンス
    else チェック成功
        バックエンド->>データベース: カテゴリ情報更新
        activate データベース
        データベース-->>バックエンド: 更新後のカテゴリ情報
        deactivate データベース
        バックエンド-->>フロントエンド: 200 OK (更新後カテゴリ情報)
    end
    deactivate バックエンド
```

### カテゴリ削除
```mermaid
sequenceDiagram
    participant フロントエンド
    participant バックエンド
    participant Firebase Auth
    participant データベース

    フロントエンド->>バックエンド: DELETE /categories/{id} <br> (IDトークン)
    activate バックエンド
    バックエンド->>Firebase Auth: IDトークン検証
    Firebase Auth-->>バックエンド: 検証結果 (UID)

    note over バックエンド: ① 権限・存在チェック <br> ② 紐づくアイテムの存在チェック

    alt チェック失敗
        バックエンド-->>フロントエンド: 403, 404, 409 エラーレスポンス
    else チェック成功
        バックエンド->>データベース: カテゴリを論理削除
        activate データベース
        データベース-->>バックエンド: 更新成功
        deactivate データベース
        バックエンド-->>フロントエンド: 204 No Content
    end
    deactivate バックエンド
```

## アイテム管理
### アイテム一覧
```mermaid
sequenceDiagram
    participant フロントエンド
    participant バックエンド
    participant Firebase Auth
    participant データベース

    フロントエンド->>バックエンド: GET /items?categoryId={id} <br> (IDトークン)
    activate バックエンド
    バックエンド->>Firebase Auth: IDトークン検証
    Firebase Auth-->>バックエンド: 検証結果 (UID)

    バックエンド->>データベース: カテゴリ存在チェック (categoryId)
    activate データベース

    alt カテゴリ存在なし
        データベース-->>バックエンド: 存在しない
        バックエンド-->>フロントエンド: 404 Not Found
    else カテゴリ存在あり
        データベース-->>バックエンド: 存在する
        バックエンド->>データベース: アイテム一覧取得 (categoryId, UID)
        データベース-->>バックエンド: アイテム一覧
        deactivate データベース
        バックエンド-->>フロントエンド: 200 OK (アイテム一覧)
    end
    deactivate バックエンド


```

### アイテム新規作成
```mermaid
sequenceDiagram
    participant フロントエンド
    participant バックエンド
    participant Firebase Auth
    participant データベース

    フロントエンド->>バックエンド: POST /items <br> (IDトークン, {name, categoryId, ...})
    activate バックエンド
    バックエンド->>Firebase Auth: IDトークン検証
    Firebase Auth-->>バックエンド: 検証結果 (UID)

    note over バックエンド: バリデーション (カテゴリ存在・権限,アイテム名,数量,重複)

    alt バリデーション失敗
        バックエンド-->>フロントエンド: 400, 404, 409 エラーレスポンス
    else バリデーション成功
        バックエンド->>データベース: 新規アイテム登録
        activate データベース
        データベース-->>バックエンド: 登録したアイテム情報
        deactivate データベース
        バックエンド-->>フロントエンド: 201 Created (アイテム情報)
    end
    deactivate バックエンド

```

### アイテム編集
```mermaid
sequenceDiagram
    participant フロントエンド
    participant バックエンド
    participant Firebase Auth
    participant データベース

    フロントエンド->>バックエンド: PATCH /items/{id} <br> (IDトークン, {name, quantity, ...})
    activate バックエンド
    バックエンド->>Firebase Auth: IDトークン検証
    Firebase Auth-->>バックエンド: 検証結果 (UID)

    note over バックエンド: ① 権限・存在チェック <br> ② バリデーション (必須,文字数,数量,重複)

    alt チェック失敗
        バックエンド-->>フロントエンド: 400, 403, 404, 409 エラーレスポンス
    else チェック成功
        バックエンド->>データベース: アイテム情報更新
        activate データベース
        データベース-->>バックエンド: 更新後のカテゴリ情報
        deactivate データベース
        バックエンド-->>フロントエンド: 200 OK (更新後カテゴリ情報)
    end
    deactivate バックエンド

```

### アイテム削除
```mermaid
sequenceDiagram
    participant フロントエンド
    participant バックエンド
    participant Firebase Auth
    participant データベース

    フロントエンド->>バックエンド: DELETE /items/{id} <br> (IDトークン)
    activate バックエンド
    バックエンド->>Firebase Auth: IDトークン検証
    Firebase Auth-->>バックエンド: 検証結果 (UID)

    note over バックエンド: 権限・存在チェック

    alt チェック失敗
        バックエンド-->>フロントエンド: 403, 404 エラーレスポンス
    else チェック成功
        バックエンド->>データベース: アイテムを論理削除
        activate データベース
        データベース-->>バックエンド: 更新成功
        deactivate データベース
        バックエンド-->>フロントエンド: 204 No Content
    end
    deactivate バックエンド
```
