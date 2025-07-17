INSERT INTO user (id, nickname, provider, provider_id, last_login)
VALUES (1, 'test-user', 'kakao', 'kakao-1234', NOW());

-- INSERT INTO quiz (
--     title,
--     description,
--     quiz_type,
--     thumbnail_url,
--     creator_id
-- ) VALUES (
--              '기본 상식 퀴즈',
--              '일반 상식을 테스트하는 쉬운 퀴즈입니다.',
--              'TEXT',             -- Enum 타입: QuizType에 따라 값 조정
--              'https://example.com/thumbnail.png',
--              1
--          );