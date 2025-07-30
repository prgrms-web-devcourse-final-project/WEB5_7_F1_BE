CREATE TABLE admin (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL,
    password VARCHAR(20) NOT NULL,
    last_login DATETIME(6) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE `user` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nickname varchar(10),
    provider varchar(10) NOT NULL,
    provider_id varchar(20) NOT NULL,
    last_login DATETIME(6) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE stat (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    total_games BIGINT NOT NULL,
    winning_games BIGINT NOT NULL,
    score BIGINT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE quiz (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(40) NOT NULL,
    quiz_type VARCHAR(10) NOT NULL,
    creator_id BIGINT,
    description VARCHAR(60) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    quiz_id BIGINT NOT NULL,
    answer VARCHAR(40) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE text_question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    content varchar(40) NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE stat ADD CONSTRAINT UK_stat__user_id UNIQUE (user_id);

ALTER TABLE text_question ADD CONSTRAINT UK_text_question__question_id UNIQUE (question_id);

ALTER TABLE `user` ADD CONSTRAINT UK_user__nickname unique (nickname);

ALTER TABLE question ADD CONSTRAINT FK_question__quiz_id
    FOREIGN KEY (quiz_id) REFERENCES quiz (id);

ALTER TABLE quiz ADD CONSTRAINT FK_quiz__creator_id
    FOREIGN KEY (creator_id) REFERENCES `user` (id);

ALTER TABLE stat ADD CONSTRAINT FK_stat__user_id
    FOREIGN KEY (user_id) REFERENCES `user` (id);

ALTER TABLE text_question ADD CONSTRAINT FK_test_question__question_id
    FOREIGN KEY (question_id) REFERENCES question (id);