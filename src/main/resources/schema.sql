-- categories テーブル
CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT,  -- 主キー、自動採番
    user_id VARCHAR(128),  -- UIDは最大128文字
    name VARCHAR(50) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- items テーブル
CREATE TABLE items (
    id INT PRIMARY KEY AUTO_INCREMENT,  -- 主キー、自動採番
    category_id INT NOT NULL,
    user_id VARCHAR(128),
    name VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    price INT,
    place VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)  -- 外部キーの設定と関連づけ
);
