INSERT INTO user (id, nickname, provider, provider_id, last_login)
VALUES (1, 'test-user', 'kakao', 'kakao-1234', NOW());


INSERT INTO quiz (title,
                  description,
                  quiz_type,
                  thumbnail_url,
                  creator_id)
VALUES ('기본 상식 퀴즈',
        '일반 상식을 테스트하는 쉬운 퀴즈입니다.',
        'TEXT',
        'https://example.com/thumbnail.png',
        1);

INSERT INTO question (quiz_id, answer, created_at, updated_at)
VALUES
    (1, '정답1', NOW(), NOW()),
    (1, '정답2', NOW(), NOW()),
    (1, '정답3', NOW(), NOW()),
    (1, '정답4', NOW(), NOW()),
    (1, '정답5', NOW(), NOW()),
    (1, '정답6', NOW(), NOW()),
    (1, '정답7', NOW(), NOW()),
    (1, '정답8', NOW(), NOW()),
    (1, '정답9', NOW(), NOW()),
    (1, '정답10', NOW(), NOW());