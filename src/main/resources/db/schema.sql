-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    id_card VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    status INTEGER DEFAULT 0,
    role VARCHAR(20) DEFAULT 'USER',
    create_time TEXT DEFAULT (datetime('now', 'localtime')),
    update_time TEXT DEFAULT (datetime('now', 'localtime')),
    cancel_time TEXT
);

-- 登录记录表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    username VARCHAR(50) NOT NULL,
    login_time TEXT DEFAULT (datetime('now', 'localtime')),
    ip_address VARCHAR(50),
    login_status INTEGER DEFAULT 1,
    message VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_user_id ON sys_login_log(user_id);
CREATE INDEX IF NOT EXISTS idx_login_time ON sys_login_log(login_time);
