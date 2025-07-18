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
        'https://picsum.photos/200/300',
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

INSERT INTO text_question (question_id, content)
VALUES
    (1, '1번 문제 내용입니다.'),
    (2, '2번 문제 내용입니다.'),
    (3, '3번 문제 내용입니다.'),
    (4, '4번 문제 내용입니다.'),
    (5, '5번 문제 내용입니다.'),
    (6, '6번 문제 내용입니다.'),
    (7, '7번 문제 내용입니다.'),
    (8, '8번 문제 내용입니다.'),
    (9, '9번 문제 내용입니다.'),
    (10, '10번 문제 내용입니다.');