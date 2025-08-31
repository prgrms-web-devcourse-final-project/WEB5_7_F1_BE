-- text_question 테이블을 content_question으로 변경
-- 1. 기존 제약조건 삭제
ALTER TABLE text_question DROP CONSTRAINT FK_test_question__question_id;
ALTER TABLE text_question DROP CONSTRAINT UK_text_question__question_id;

-- 2. 테이블 이름 변경
RENAME TABLE text_question TO content_question;

-- 3. 새로운 제약조건 추가
ALTER TABLE content_question ADD CONSTRAINT UK_content_question__question_id UNIQUE (question_id);
ALTER TABLE content_question ADD CONSTRAINT FK_content_question__question_id
    FOREIGN KEY (question_id) REFERENCES question (id);
