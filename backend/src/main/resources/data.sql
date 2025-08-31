INSERT INTO user (nickname, provider, provider_id, last_login)
VALUES
    ('blueFox17', 'kakao', 'kakao-1', NOW()),
    ('sparkLion23', 'kakao', 'kakao-2', NOW()),
    ('novaTiger88', 'kakao', 'kakao-3', NOW()),
    ('mintWolf5', 'kakao', 'kakao-4', NOW()),
    ('darkFalcon32', 'kakao', 'kakao-5', NOW()),
    ('chocoBear91', 'kakao', 'kakao-6', NOW()),
    ('swiftOtter3', 'kakao', 'kakao-7', NOW()),
    ('crazyKoala47', 'kakao', 'kakao-8', NOW()),
    ('luckyWhale19', 'kakao', 'kakao-9', NOW()),
    ('tinyMoose7', 'kakao', 'kakao-10', NOW()),
    ('sunnyLizard22', 'kakao', 'kakao-11', NOW()),
    ('fuzzyCat12', 'kakao', 'kakao-12', NOW()),
    ('stormEagle9', 'kakao', 'kakao-13', NOW()),
    ('frostRabbit6', 'kakao', 'kakao-14', NOW()),
    ('amberGoose99', 'kakao', 'kakao-15', NOW()),
    ('wittyDog42', 'kakao', 'kakao-16', NOW()),
    ('zebraKnight77', 'kakao', 'kakao-17', NOW()),
    ('happyDeer13', 'kakao', 'kakao-18', NOW()),
    ('ironTurtle55', 'kakao', 'kakao-19', NOW()),
    ('lazyHawk31', 'kakao', 'kakao-20', NOW()),
    ('pixelPanda8', 'kakao', 'kakao-21', NOW()),
    ('grapeSeal66', 'kakao', 'kakao-22', NOW()),
    ('redMole21', 'kakao', 'kakao-23', NOW()),
    ('lunaShark29', 'kakao', 'kakao-24', NOW()),
    ('noblePiglet45', 'kakao', 'kakao-25', NOW()),
    ('quietYak17', 'kakao', 'kakao-26', NOW()),
    ('jumboDuck33', 'kakao', 'kakao-27', NOW()),
    ('rapidWorm11', 'kakao', 'kakao-28', NOW()),
    ('boldSwan73', 'kakao', 'kakao-29', NOW()),
    ('vividAnt9', 'kakao', 'kakao-30', NOW());

INSERT INTO stat (user_id, score, total_games, winning_games, created_at, updated_at)
VALUES
    (1, 10, 1, 1, NOW(), NOW()),
    (2, 20, 3, 2, NOW(), NOW()),
    (3, 15, 2, 1, NOW(), NOW()),
    (4, 5, 1, 0, NOW(), NOW()),
    (5, 25, 4, 3, NOW(), NOW()),
    (6, 12, 2, 1, NOW(), NOW()),
    (7, 18, 3, 2, NOW(), NOW()),
    (8, 30, 5, 4, NOW(), NOW()),
    (9, 7, 1, 0, NOW(), NOW()),
    (10, 22, 3, 2, NOW(), NOW()),
    (11, 16, 2, 1, NOW(), NOW()),
    (12, 9, 1, 0, NOW(), NOW()),
    (13, 27, 4, 3, NOW(), NOW()),
    (14, 14, 2, 1, NOW(), NOW()),
    (15, 8, 1, 1, NOW(), NOW()),
    (16, 23, 3, 2, NOW(), NOW()),
    (17, 11, 2, 1, NOW(), NOW()),
    (18, 19, 3, 2, NOW(), NOW()),
    (19, 6, 1, 0, NOW(), NOW()),
    (20, 28, 4, 3, NOW(), NOW()),
    (21, 13, 2, 1, NOW(), NOW()),
    (22, 10, 1, 1, NOW(), NOW()),
    (23, 21, 3, 2, NOW(), NOW()),
    (24, 17, 2, 1, NOW(), NOW()),
    (25, 24, 4, 3, NOW(), NOW()),
    (26, 29, 5, 4, NOW(), NOW()),
    (27, 26, 4, 3, NOW(), NOW()),
    (28, 31, 5, 4, NOW(), NOW()),
    (29, 32, 5, 4, NOW(), NOW()),
    (30, 33, 5, 5, NOW(), NOW());


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

INSERT INTO content_question (question_id, content)
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

-- 퀴즈 2 ~ 21 추가
INSERT INTO quiz (title, description, quiz_type, thumbnail_url, creator_id) VALUES
                                                                                ('세계 수도 퀴즈', '나라의 수도를 맞혀보는 퀴즈입니다.', 'TEXT', 'https://picsum.photos/seed/2/200/300', 1),
                                                                                ('동물 상식 퀴즈', '동물에 대한 재미있는 상식을 알아보세요.', 'TEXT', 'https://picsum.photos/seed/3/200/300', 1),
                                                                                ('과학 기초 퀴즈', '과학 기초 개념을 테스트합니다.', 'TEXT', 'https://picsum.photos/seed/4/200/300', 1),
                                                                                ('역사 인물 퀴즈', '유명한 역사 인물들을 맞혀보세요.', 'TEXT', 'https://picsum.photos/seed/5/200/300', 1),
                                                                                ('스포츠 상식 퀴즈', '스포츠에 관한 기본 지식을 묻는 퀴즈입니다.', 'TEXT', 'https://picsum.photos/seed/6/200/300', 1),
                                                                                ('문학 작품 퀴즈', '유명한 문학 작품과 작가를 묻는 퀴즈입니다.', 'TEXT', 'https://picsum.photos/seed/7/200/300', 1),
                                                                                ('음악 상식 퀴즈', '음악 이론과 아티스트에 대해 맞혀보세요.', 'TEXT', 'https://picsum.photos/seed/8/200/300', 1),
                                                                                ('IT 기초 퀴즈', 'IT 기초 개념과 용어를 묻는 퀴즈입니다.', 'TEXT', 'https://picsum.photos/seed/9/200/300', 1),
                                                                                ('한국 지리 퀴즈', '대한민국의 지리 상식을 테스트합니다.', 'TEXT', 'https://picsum.photos/seed/10/200/300', 1),
                                                                                ('세계 음식 퀴즈', '다양한 국가의 음식 이름을 맞혀보세요.', 'TEXT', 'https://picsum.photos/seed/11/200/300', 1),
                                                                                ('속담 퀴즈', '한국 속담의 빈칸을 채워보세요.', 'TEXT', 'https://picsum.photos/seed/12/200/300', 1),
                                                                                ('관용어 퀴즈', '국어 관용어 표현을 테스트하는 퀴즈입니다.', 'TEXT', 'https://picsum.photos/seed/13/200/300', 1),
                                                                                ('물리 법칙 퀴즈', '물리학의 기본 법칙에 대해 알아보세요.', 'TEXT', 'https://picsum.photos/seed/14/200/300', 1),
                                                                                ('화학 원소 퀴즈', '주기율표 원소에 관한 퀴즈입니다.', 'TEXT', 'https://picsum.photos/seed/15/200/300', 1),
                                                                                ('영화 제목 퀴즈', '유명 영화의 제목을 맞혀보세요.', 'TEXT', 'https://picsum.photos/seed/16/200/300', 1),
                                                                                ('명언 출처 퀴즈', '유명한 명언의 출처를 맞혀보세요.', 'TEXT', 'https://picsum.photos/seed/17/200/300', 1),
                                                                                ('한국사 연도 퀴즈', '한국사의 주요 사건 연도를 묻습니다.', 'TEXT', 'https://picsum.photos/seed/18/200/300', 1),
                                                                                ('생활 속 수학 퀴즈', '생활 속에서 활용되는 수학 개념 퀴즈입니다.', 'TEXT', 'https://picsum.photos/seed/19/200/300', 1),
                                                                                ('논리 퀴즈', '간단한 논리 문제를 풀어보세요.', 'TEXT', 'https://picsum.photos/seed/20/200/300', 1),
                                                                                ('영어 단어 퀴즈', '기본적인 영어 단어를 테스트하는 퀴즈입니다.', 'TEXT', 'https://picsum.photos/seed/21/200/300', 1);

-- 퀴즈별 문제들 추가 (퀴즈 2~21, 각 퀴즈당 문제 개수 10~80개 랜덤)
-- 예시로 퀴즈 2 (퀴즈 ID 2번)부터 3개만 우선 생성해드릴게요. 전체 20개 모두 원하시면 이어서 계속 드릴게요.

-- 퀴즈 2: 세계 수도 퀴즈 (15문제)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (2, '파리', NOW(), NOW()),
                                                                   (2, '도쿄', NOW(), NOW()),
                                                                   (2, '베이징', NOW(), NOW()),
                                                                   (2, '카이로', NOW(), NOW()),
                                                                   (2, '런던', NOW(), NOW()),
                                                                   (2, '마드리드', NOW(), NOW()),
                                                                   (2, '로마', NOW(), NOW()),
                                                                   (2, '오타와', NOW(), NOW()),
                                                                   (2, '캔버라', NOW(), NOW()),
                                                                   (2, '방콕', NOW(), NOW()),
                                                                   (2, '하노이', NOW(), NOW()),
                                                                   (2, '자카르타', NOW(), NOW()),
                                                                   (2, '베를린', NOW(), NOW()),
                                                                   (2, '리스본', NOW(), NOW()),
                                                                   (2, '서울', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (11, '프랑스의 수도는 어디인가요?'),
                                                     (12, '일본의 수도는 어디인가요?'),
                                                     (13, '중국의 수도는 어디인가요?'),
                                                     (14, '이집트의 수도는 어디인가요?'),
                                                     (15, '영국의 수도는 어디인가요?'),
                                                     (16, '스페인의 수도는 어디인가요?'),
                                                     (17, '이탈리아의 수도는 어디인가요?'),
                                                     (18, '캐나다의 수도는 어디인가요?'),
                                                     (19, '호주의 수도는 어디인가요?'),
                                                     (20, '태국의 수도는 어디인가요?'),
                                                     (21, '베트남의 수도는 어디인가요?'),
                                                     (22, '인도네시아의 수도는 어디인가요?'),
                                                     (23, '독일의 수도는 어디인가요?'),
                                                     (24, '포르투갈의 수도는 어디인가요?'),
                                                     (25, '대한민국의 수도는 어디인가요?');

-- 퀴즈 3: 동물 상식 퀴즈 (12문제)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (3, '코끼리', NOW(), NOW()),
                                                                   (3, '기린', NOW(), NOW()),
                                                                   (3, '하마', NOW(), NOW()),
                                                                   (3, '펭귄', NOW(), NOW()),
                                                                   (3, '고래', NOW(), NOW()),
                                                                   (3, '호랑이', NOW(), NOW()),
                                                                   (3, '늑대', NOW(), NOW()),
                                                                   (3, '표범', NOW(), NOW()),
                                                                   (3, '캥거루', NOW(), NOW()),
                                                                   (3, '타조', NOW(), NOW()),
                                                                   (3, '침팬지', NOW(), NOW()),
                                                                   (3, '코알라', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (26, '가장 큰 육상 동물은?'),
                                                     (27, '목이 가장 긴 동물은?'),
                                                     (28, '물속에서 오래 숨을 참을 수 있는 동물은?'),
                                                     (29, '날지 못하지만 헤엄을 잘 치는 남극 동물은?'),
                                                     (30, '포유류 중에서 가장 큰 동물은?'),
                                                     (31, '한국의 대표적인 맹수는?'),
                                                     (32, '개과 동물로 무리를 지어 사냥하는 동물은?'),
                                                     (33, '나무 위에서 생활하는 표범 종류는?'),
                                                     (34, '아기를 주머니에 넣고 다니는 동물은?'),
                                                     (35, '날지 못하는 세계에서 가장 큰 새는?'),
                                                     (36, '인간과 DNA가 가장 비슷한 동물은?'),
                                                     (37, '호주의 대표 동물 중 나무에 사는 귀여운 동물은?');

-- 퀴즈 4: 과학 기초 퀴즈 (10문제)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (4, '물', NOW(), NOW()),
                                                                   (4, '태양', NOW(), NOW()),
                                                                   (4, '빛의 속도', NOW(), NOW()),
                                                                   (4, '뉴턴', NOW(), NOW()),
                                                                   (4, '중력', NOW(), NOW()),
                                                                   (4, '산소', NOW(), NOW()),
                                                                   (4, '수소', NOW(), NOW()),
                                                                   (4, '지구', NOW(), NOW()),
                                                                   (4, '탄소', NOW(), NOW()),
                                                                   (4, '전자', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (38, '화학식 H2O는 무엇인가요?'),
                                                     (39, '지구에 빛과 열을 제공하는 별은?'),
                                                     (40, '우주에서 가장 빠른 것은?'),
                                                     (41, '운동 법칙을 정리한 과학자는?'),
                                                     (42, '물체를 지구로 끌어당기는 힘은?'),
                                                     (43, '인간이 숨쉴 때 필요한 기체는?'),
                                                     (44, '가장 가벼운 원소는?'),
                                                     (45, '우리가 사는 행성은?'),
                                                     (46, '생명체의 주된 구성 원소 중 하나는?'),
                                                     (47, '원자핵 주위를 도는 입자는?');

-- 퀴즈 5: 역사 인물 퀴즈 (문제 13개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (5, '세종대왕', NOW(), NOW()),
                                                                   (5, '이순신', NOW(), NOW()),
                                                                   (5, '율곡 이이', NOW(), NOW()),
                                                                   (5, '유관순', NOW(), NOW()),
                                                                   (5, '간디', NOW(), NOW()),
                                                                   (5, '나폴레옹', NOW(), NOW()),
                                                                   (5, '히틀러', NOW(), NOW()),
                                                                   (5, '마틴 루터 킹', NOW(), NOW()),
                                                                   (5, '링컨', NOW(), NOW()),
                                                                   (5, '알렉산더 대왕', NOW(), NOW()),
                                                                   (5, '클레오파트라', NOW(), NOW()),
                                                                   (5, '레오나르도 다 빈치', NOW(), NOW()),
                                                                   (5, '아인슈타인', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (48, '한글을 창제한 조선의 왕은?'),
                                                     (49, '임진왜란 당시 활약한 조선의 장군은?'),
                                                     (50, '10만 양병설로 유명한 조선의 학자는?'),
                                                     (51, '3.1운동에 참여한 여성 독립운동가는?'),
                                                     (52, '비폭력 저항 운동으로 인도 독립을 이끈 인물은?'),
                                                     (53, '프랑스 황제로 유럽 정복을 시도한 인물은?'),
                                                     (54, '제2차 세계대전의 독일 총통은?'),
                                                     (55, '"I have a dream" 연설로 유명한 인권 운동가는?'),
                                                     (56, '미국의 노예 해방을 선언한 대통령은?'),
                                                     (57, '마케도니아 출신의 세계 정복자는?'),
                                                     (58, '고대 이집트의 여왕으로 유명한 인물은?'),
                                                     (59, '모나리자를 그린 예술가는?'),
                                                     (60, '상대성 이론을 제안한 과학자는?');

-- 퀴즈 6: 스포츠 상식 퀴즈 (문제 11개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (6, '축구', NOW(), NOW()),
                                                                   (6, '야구', NOW(), NOW()),
                                                                   (6, '농구', NOW(), NOW()),
                                                                   (6, '배구', NOW(), NOW()),
                                                                   (6, '골프', NOW(), NOW()),
                                                                   (6, '테니스', NOW(), NOW()),
                                                                   (6, '복싱', NOW(), NOW()),
                                                                   (6, '씨름', NOW(), NOW()),
                                                                   (6, '마라톤', NOW(), NOW()),
                                                                   (6, '탁구', NOW(), NOW()),
                                                                   (6, '수영', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (61, '11명이 한 팀으로 경기하는 스포츠는?'),
                                                     (62, '홈런이 나오는 스포츠는?'),
                                                     (63, '슛과 리바운드가 중요한 스포츠는?'),
                                                     (64, '서브와 블로킹이 중요한 스포츠는?'),
                                                     (65, '홀인원이 가능한 스포츠는?'),
                                                     (66, '라켓으로 치고 네트를 넘기는 스포츠는?'),
                                                     (67, 'KO 승부가 있는 격투 스포츠는?'),
                                                     (68, '한국 전통적인 힘겨루기 스포츠는?'),
                                                     (69, '42.195km를 달리는 경기 종목은?'),
                                                     (70, '라켓으로 작은 공을 빠르게 치는 실내 스포츠는?'),
                                                     (71, '자유형, 배영, 평영 등이 있는 스포츠는?');

-- 퀴즈 7: 문학 작품 퀴즈 (문제 14개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (7, '이광수', NOW(), NOW()),
                                                                   (7, '김소월', NOW(), NOW()),
                                                                   (7, '이상', NOW(), NOW()),
                                                                   (7, '한강', NOW(), NOW()),
                                                                   (7, '도스토예프스키', NOW(), NOW()),
                                                                   (7, '셰익스피어', NOW(), NOW()),
                                                                   (7, '카프카', NOW(), NOW()),
                                                                   (7, '헤밍웨이', NOW(), NOW()),
                                                                   (7, '조지 오웰', NOW(), NOW()),
                                                                   (7, '허먼 멜빌', NOW(), NOW()),
                                                                   (7, '톨스토이', NOW(), NOW()),
                                                                   (7, '괴테', NOW(), NOW()),
                                                                   (7, '박완서', NOW(), NOW()),
                                                                   (7, '루이자 메이 올컷', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (72, '『무정』을 쓴 작가는 누구인가요?'),
                                                     (73, '『진달래꽃』을 지은 시인은 누구인가요?'),
                                                     (74, '『날개』를 쓴 작가는 누구인가요?'),
                                                     (75, '『채식주의자』로 맨부커상을 수상한 한국 작가는?'),
                                                     (76, '『죄와 벌』을 쓴 러시아 작가는?'),
                                                     (77, '『햄릿』과 『로미오와 줄리엣』의 작가는?'),
                                                     (78, '『변신』을 쓴 작가는 누구인가요?'),
                                                     (79, '『노인과 바다』의 작가는?'),
                                                     (80, '『1984』와 『동물농장』을 쓴 작가는?'),
                                                     (81, '『모비딕』의 작가는 누구인가요?'),
                                                     (82, '『전쟁과 평화』를 쓴 러시아 문호는?'),
                                                     (83, '『젊은 베르테르의 슬픔』을 쓴 독일 작가는?'),
                                                     (84, '『엄마의 말뚝』을 쓴 작가는?'),
                                                     (85, '『작은 아씨들』을 쓴 작가는?');

-- 퀴즈 8: 음악 상식 퀴즈 (문제 10개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (8, '피아노', NOW(), NOW()),
                                                                   (8, '바이올린', NOW(), NOW()),
                                                                   (8, '베토벤', NOW(), NOW()),
                                                                   (8, '모차르트', NOW(), NOW()),
                                                                   (8, '쇼팽', NOW(), NOW()),
                                                                   (8, '드럼', NOW(), NOW()),
                                                                   (8, '기타', NOW(), NOW()),
                                                                   (8, '악보', NOW(), NOW()),
                                                                   (8, '음표', NOW(), NOW()),
                                                                   (8, '템포', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (86, '88개의 건반이 있는 대표적인 건반악기는?'),
                                                     (87, '활을 사용하는 대표적인 현악기는?'),
                                                     (88, '『운명 교향곡』을 작곡한 사람은?'),
                                                     (89, '『마술피리』를 작곡한 천재 음악가는?'),
                                                     (90, '『야상곡』으로 유명한 작곡가는?'),
                                                     (91, '리듬을 만드는 대표적인 타악기는?'),
                                                     (92, '스트링과 프렛이 있는 악기는?'),
                                                     (93, '음의 높낮이와 길이를 적는 음악 기호는?'),
                                                     (94, '소리의 길이를 나타내는 기호는?'),
                                                     (95, '빠르기를 나타내는 음악 용어는?');


-- 퀴즈 9: 한국 지리 퀴즈 (문제 10개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (9, '한라산', NOW(), NOW()),
                                                                   (9, '백두산', NOW(), NOW()),
                                                                   (9, '낙동강', NOW(), NOW()),
                                                                   (9, '한강', NOW(), NOW()),
                                                                   (9, '서울', NOW(), NOW()),
                                                                   (9, '제주도', NOW(), NOW()),
                                                                   (9, '부산', NOW(), NOW()),
                                                                   (9, '강원도', NOW(), NOW()),
                                                                   (9, '울릉도', NOW(), NOW()),
                                                                   (9, '독도', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (96, '대한민국에서 가장 높은 산은?'),
                                                     (97, '한반도의 가장 북쪽에 있는 산은?'),
                                                     (98, '대한민국에서 가장 긴 강은?'),
                                                     (99, '서울을 관통하는 강은?'),
                                                     (100, '대한민국의 수도는?'),
                                                     (101, '화산섬으로 유명한 섬은?'),
                                                     (102, '대한민국 제2의 도시는?'),
                                                     (103, '평창이 위치한 도는?'),
                                                     (104, '독도와 가장 가까운 섬은?'),
                                                     (105, '우리나라 영토로 분쟁이 있는 섬은?');

-- 퀴즈 10: 세계 음식 퀴즈 (문제 12개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (10, '김치', NOW(), NOW()),
                                                                   (10, '스시', NOW(), NOW()),
                                                                   (10, '파스타', NOW(), NOW()),
                                                                   (10, '타코', NOW(), NOW()),
                                                                   (10, '크로아상', NOW(), NOW()),
                                                                   (10, '카레', NOW(), NOW()),
                                                                   (10, '딤섬', NOW(), NOW()),
                                                                   (10, '파에야', NOW(), NOW()),
                                                                   (10, '브랏부어스트', NOW(), NOW()),
                                                                   (10, '햄버거', NOW(), NOW()),
                                                                   (10, '포케', NOW(), NOW()),
                                                                   (10, '무사카', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (106, '한국을 대표하는 발효 음식은?'),
                                                     (107, '일본의 생선 초밥을 무엇이라 하나요?'),
                                                     (108, '이탈리아의 대표적인 면 요리는?'),
                                                     (109, '멕시코 전통 음식으로, 또띠아에 재료를 싸서 먹는 음식은?'),
                                                     (110, '프랑스의 대표적인 빵은?'),
                                                     (111, '인도의 대표 향신료 요리는?'),
                                                     (112, '중국의 대표적인 한입 크기 음식은?'),
                                                     (113, '스페인의 해산물 볶음밥은?'),
                                                     (114, '독일의 대표적인 소시지는?'),
                                                     (115, '미국의 대표적인 패스트푸드는?'),
                                                     (116, '하와이에서 유래한 생선덮밥은?'),
                                                     (117, '그리스의 전통 가지요리는?');

-- 퀴즈 11: 속담 퀴즈 (문제 13개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (11, '말', NOW(), NOW()),
                                                                   (11, '소', NOW(), NOW()),
                                                                   (11, '호랑이', NOW(), NOW()),
                                                                   (11, '고래', NOW(), NOW()),
                                                                   (11, '바늘', NOW(), NOW()),
                                                                   (11, '도둑', NOW(), NOW()),
                                                                   (11, '떡', NOW(), NOW()),
                                                                   (11, '금', NOW(), NOW()),
                                                                   (11, '개구리', NOW(), NOW()),
                                                                   (11, '도토리', NOW(), NOW()),
                                                                   (11, '낮말', NOW(), NOW()),
                                                                   (11, '돌다리', NOW(), NOW()),
                                                                   (11, '벼', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (118, '가는 말이 고와야 오는 ○○○ 곱다'),
                                                     (119, '소 잃고 ○○○ 고친다'),
                                                     (120, '호랑이도 ○○○ 앞에서는 없다'),
                                                     (121, '고래 싸움에 ○○○ 등 터진다'),
                                                     (122, '바늘 도둑이 ○○○ 도둑 된다'),
                                                     (123, '도둑이 제 ○○○ 찔린다'),
                                                     (124, '보기 좋은 ○○○이 먹기도 좋다'),
                                                     (125, '시간은 ○○이다'),
                                                     (126, '개구리 올챙이 적 생각 못 한다'),
                                                     (127, '키 작은 사람이 ○○ 키 재기'),
                                                     (128, '○○은 새가 듣고 밤말은 쥐가 듣는다'),
                                                     (129, '○○○도 두들겨 보고 건너라'),
                                                     (130, '○○ 이삭이 고개를 숙인다');

-- 퀴즈 12: 관용어 퀴즈 (문제 11개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (12, '입', NOW(), NOW()),
                                                                   (12, '눈', NOW(), NOW()),
                                                                   (12, '손', NOW(), NOW()),
                                                                   (12, '귀', NOW(), NOW()),
                                                                   (12, '발', NOW(), NOW()),
                                                                   (12, '속', NOW(), NOW()),
                                                                   (12, '코', NOW(), NOW()),
                                                                   (12, '입맛', NOW(), NOW()),
                                                                   (12, '머리', NOW(), NOW()),
                                                                   (12, '목', NOW(), NOW()),
                                                                   (12, '배', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (131, '○이 가볍다 = 말이 많다'),
                                                     (132, '○에 불이 나다 = 매우 바쁘다'),
                                                     (133, '○에 장난 치다 = 방해하다'),
                                                     (134, '○이 얇다 = 참견을 잘한다'),
                                                     (135, '○이 닳도록 빌다 = 간절히 사과하다'),
                                                     (136, '○이 상하다 = 기분이 나쁘다'),
                                                     (137, '○를 굴리다 = 생각하다'),
                                                     (138, '○을 걸다 = 강하게 주장하다'),
                                                     (139, '○가 아프다 = 부러워하다'),
                                                     (140, '○이 끊기다 = 매우 화가 나다'),
                                                     (141, '○가 부르다 = 만족하다');


-- 퀴즈 13: 물리 법칙 퀴즈 (문제 12개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (13, '뉴턴 제1법칙', NOW(), NOW()),
                                                                   (13, '뉴턴 제2법칙', NOW(), NOW()),
                                                                   (13, '뉴턴 제3법칙', NOW(), NOW()),
                                                                   (13, '중력', NOW(), NOW()),
                                                                   (13, '속도', NOW(), NOW()),
                                                                   (13, '가속도', NOW(), NOW()),
                                                                   (13, '관성', NOW(), NOW()),
                                                                   (13, '힘', NOW(), NOW()),
                                                                   (13, '일', NOW(), NOW()),
                                                                   (13, '에너지 보존 법칙', NOW(), NOW()),
                                                                   (13, '작용 반작용', NOW(), NOW()),
                                                                   (13, '운동량 보존 법칙', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (142, '정지한 물체는 계속 정지하고, 운동 중인 물체는 계속 운동하려는 법칙은?'),
                                                     (143, '힘 = 질량 x 가속도, 어떤 법칙인가요?'),
                                                     (144, '모든 작용에는 크기가 같고 방향이 반대인 반작용이 따른다. 어떤 법칙인가요?'),
                                                     (145, '지구가 물체를 끌어당기는 힘은?'),
                                                     (146, '단위 시간당 위치 변화는?'),
                                                     (147, '속도의 변화율은?'),
                                                     (148, '물체가 운동 상태를 유지하려는 성질은?'),
                                                     (149, '질량과 가속도의 곱으로 정의되는 물리량은?'),
                                                     (150, '힘 x 거리로 정의되는 물리량은?'),
                                                     (151, '고립계에서 전체 에너지는 일정하다는 법칙은?'),
                                                     (152, '힘을 가하면 반대 방향으로 같은 크기의 힘이 생기는 현상은?'),
                                                     (153, '운동량이 외부 힘 없이 일정하게 유지되는 법칙은?');

-- 퀴즈 14: 화학 원소 퀴즈 (문제 11개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (14, '수소', NOW(), NOW()),
                                                                   (14, '헬륨', NOW(), NOW()),
                                                                   (14, '산소', NOW(), NOW()),
                                                                   (14, '탄소', NOW(), NOW()),
                                                                   (14, '질소', NOW(), NOW()),
                                                                   (14, '철', NOW(), NOW()),
                                                                   (14, '구리', NOW(), NOW()),
                                                                   (14, '은', NOW(), NOW()),
                                                                   (14, '금', NOW(), NOW()),
                                                                   (14, '납', NOW(), NOW()),
                                                                   (14, '우라늄', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (154, '가장 가벼운 원소는?'),
                                                     (155, '풍선에 쓰이며 비활성 기체인 원소는?'),
                                                     (156, '생명 유지에 꼭 필요한 기체 원소는?'),
                                                     (157, '생명체의 주된 구성 원소로 유기화합물을 이루는 것은?'),
                                                     (158, '대기의 78%를 차지하는 기체는?'),
                                                     (159, '자석에 붙고 단단한 금속 원소는?'),
                                                     (160, '전선을 만드는 데 자주 사용되는 금속은?'),
                                                     (161, '하얀색 광택을 가진 귀금속은?'),
                                                     (162, '노란색 광택을 가진 귀금속은?'),
                                                     (163, '연하고 무거운 금속으로 배터리에 쓰이기도 하는 원소는?'),
                                                     (164, '방사성 원소로 원자력 발전에 사용되는 것은?');

-- 퀴즈 15: 영화 제목 퀴즈 (문제 13개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (15, '기생충', NOW(), NOW()),
                                                                   (15, '인셉션', NOW(), NOW()),
                                                                   (15, '어벤져스', NOW(), NOW()),
                                                                   (15, '타이타닉', NOW(), NOW()),
                                                                   (15, '쇼생크 탈출', NOW(), NOW()),
                                                                   (15, '인터스텔라', NOW(), NOW()),
                                                                   (15, '다크 나이트', NOW(), NOW()),
                                                                   (15, '노인을 위한 나라는 없다', NOW(), NOW()),
                                                                   (15, '아바타', NOW(), NOW()),
                                                                   (15, '라라랜드', NOW(), NOW()),
                                                                   (15, '매트릭스', NOW(), NOW()),
                                                                   (15, '해리포터', NOW(), NOW()),
                                                                   (15, '반지의 제왕', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (165, '봉준호 감독의 아카데미 수상작은?'),
                                                     (166, '꿈 속에서 또 다른 꿈으로 들어가는 영화는?'),
                                                     (167, '히어로들이 모여서 싸우는 마블 영화는?'),
                                                     (168, '배 침몰과 로맨스를 다룬 제임스 카메론 감독의 영화는?'),
                                                     (169, '감옥 탈출을 다룬 명작 영화는?'),
                                                     (170, '블랙홀과 시간 여행을 다룬 크리스토퍼 놀란 영화는?'),
                                                     (171, '조커가 등장하는 배트맨 영화는?'),
                                                     (172, '잔인한 살인마가 등장하는 코엔 형제의 영화는?'),
                                                     (173, '파란 피부의 외계인이 나오는 SF 영화는?'),
                                                     (174, '재즈와 사랑을 다룬 뮤지컬 영화는?'),
                                                     (175, '가상현실에서 싸우는 SF 영화는?'),
                                                     (176, '마법학교가 배경인 판타지 영화는?'),
                                                     (177, '반지를 파괴하기 위해 여행을 떠나는 이야기의 영화는?');

-- 퀴즈 16: 명언 출처 퀴즈 (문제 10개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (16, '아인슈타인', NOW(), NOW()),
                                                                   (16, '간디', NOW(), NOW()),
                                                                   (16, '마더 테레사', NOW(), NOW()),
                                                                   (16, '스티브 잡스', NOW(), NOW()),
                                                                   (16, '링컨', NOW(), NOW()),
                                                                   (16, '처칠', NOW(), NOW()),
                                                                   (16, '넬슨 만델라', NOW(), NOW()),
                                                                   (16, '마하트마 간디', NOW(), NOW()),
                                                                   (16, '이순신', NOW(), NOW()),
                                                                   (16, '세종대왕', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (178, '"상상력은 지식보다 중요하다." 라는 명언을 남긴 사람은?'),
                                                     (179, '"당신이 세상에서 보고 싶은 변화가 되어라." 라고 말한 인물은?'),
                                                     (180, '"우리는 큰 일을 할 수는 없지만, 작은 일을 큰 사랑으로 할 수 있습니다." 는 누구의 말인가요?'),
                                                     (181, '"Stay hungry, stay foolish" 는 누구의 연설에서 나온 말인가요?'),
                                                     (182, '"국민의, 국민에 의한, 국민을 위한 정부" 를 말한 미국 대통령은?'),
                                                     (183, '"피할 수 없다면, 즐겨라" 라고 한 영국 수상은?'),
                                                     (184, '"교육은 세상을 바꾸는 가장 강력한 무기다" 를 말한 사람은?'),
                                                     (185, '"비폭력 저항" 운동을 이끈 인도 독립운동가는 누구인가요?'),
                                                     (186, '"신에게는 아직 12척의 배가 있습니다" 를 말한 조선의 장군은?'),
                                                     (187, '"백성을 가르치는 것이 나라를 다스리는 근본이다" 라고 말한 조선의 왕은?');

-- 퀴즈 17: 나라 수도 맞히기 퀴즈 (문제 15개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (17, '서울', NOW(), NOW()),
                                                                   (17, '워싱턴 D.C.', NOW(), NOW()),
                                                                   (17, '도쿄', NOW(), NOW()),
                                                                   (17, '파리', NOW(), NOW()),
                                                                   (17, '베를린', NOW(), NOW()),
                                                                   (17, '런던', NOW(), NOW()),
                                                                   (17, '베이징', NOW(), NOW()),
                                                                   (17, '모스크바', NOW(), NOW()),
                                                                   (17, '마드리드', NOW(), NOW()),
                                                                   (17, '로마', NOW(), NOW()),
                                                                   (17, '오타와', NOW(), NOW()),
                                                                   (17, '카이로', NOW(), NOW()),
                                                                   (17, '하노이', NOW(), NOW()),
                                                                   (17, '방콕', NOW(), NOW()),
                                                                   (17, '자카르타', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (188, '대한민국의 수도는?'),
                                                     (189, '미국의 수도는?'),
                                                     (190, '일본의 수도는?'),
                                                     (191, '프랑스의 수도는?'),
                                                     (192, '독일의 수도는?'),
                                                     (193, '영국의 수도는?'),
                                                     (194, '중국의 수도는?'),
                                                     (195, '러시아의 수도는?'),
                                                     (196, '스페인의 수도는?'),
                                                     (197, '이탈리아의 수도는?'),
                                                     (198, '캐나다의 수도는?'),
                                                     (199, '이집트의 수도는?'),
                                                     (200, '베트남의 수도는?'),
                                                     (201, '태국의 수도는?'),
                                                     (202, '인도네시아의 수도는?');

-- 퀴즈 18: 넌센스 퀴즈 (문제 12개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (18, '하와이', NOW(), NOW()),
                                                                   (18, '이유는 이 유리해서', NOW(), NOW()),
                                                                   (18, '코끼리', NOW(), NOW()),
                                                                   (18, '엄마상', NOW(), NOW()),
                                                                   (18, '가위바위보', NOW(), NOW()),
                                                                   (18, '말대꾸', NOW(), NOW()),
                                                                   (18, '서울대', NOW(), NOW()),
                                                                   (18, '시소', NOW(), NOW()),
                                                                   (18, '도레미파솔라시도', NOW(), NOW()),
                                                                   (18, '바나나', NOW(), NOW()),
                                                                   (18, '기린', NOW(), NOW()),
                                                                   (18, '안경', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (203, '“니가 가라 ○○○”에서 ○○○에 들어갈 말은?'),
                                                     (204, '유리가 집을 나간 이유는?'),
                                                     (205, '하늘에서 떨어지는 코는?'),
                                                     (206, '코끼리 아빠는 코끼리, 그럼 엄마는?'),
                                                     (207, '손이 세 개인 게임은?'),
                                                     (208, '말이 거꾸로 말하면?'),
                                                     (209, '서울에서 제일 높은 대학은?'),
                                                     (210, '시소 타다가 떨어지면 뭐라고 할까?'),
                                                     (211, '음표 중 가장 비싼 것은?'),
                                                     (212, '가장 노란 과일은?'),
                                                     (213, '목이 제일 긴 동물은?'),
                                                     (214, '눈이 나쁜 사람이 쓰는 것은?');

-- 퀴즈 19: 세계 명소 퀴즈 (문제 10개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (19, '에펠탑', NOW(), NOW()),
                                                                   (19, '자유의 여신상', NOW(), NOW()),
                                                                   (19, '콜로세움', NOW(), NOW()),
                                                                   (19, '만리장성', NOW(), NOW()),
                                                                   (19, '피사의 사탑', NOW(), NOW()),
                                                                   (19, '타지마할', NOW(), NOW()),
                                                                   (19, '시드니 오페라 하우스', NOW(), NOW()),
                                                                   (19, '버킹엄 궁전', NOW(), NOW()),
                                                                   (19, '크레몰린 궁전', NOW(), NOW()),
                                                                   (19, '앙코르와트', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (215, '프랑스 파리의 상징적인 탑은?'),
                                                     (216, '뉴욕에 있는 유명한 동상은?'),
                                                     (217, '로마에 있는 고대 원형 경기장은?'),
                                                     (218, '중국에 있는 긴 성벽은?'),
                                                     (219, '이탈리아에 기울어진 탑은?'),
                                                     (220, '인도의 아름다운 묘는?'),
                                                     (221, '호주의 유명한 공연장은?'),
                                                     (222, '영국 왕실의 궁전은?'),
                                                     (223, '러시아 모스크바의 궁전은?'),
                                                     (224, '캄보디아의 고대 사원은?');

-- 퀴즈 20: 세계 음식 퀴즈 (문제 14개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (20, '김치', NOW(), NOW()),
                                                                   (20, '스시', NOW(), NOW()),
                                                                   (20, '피자', NOW(), NOW()),
                                                                   (20, '타코', NOW(), NOW()),
                                                                   (20, '파에야', NOW(), NOW()),
                                                                   (20, '크루아상', NOW(), NOW()),
                                                                   (20, '딤섬', NOW(), NOW()),
                                                                   (20, '함버거', NOW(), NOW()),
                                                                   (20, '카레', NOW(), NOW()),
                                                                   (20, '똠얌꿍', NOW(), NOW()),
                                                                   (20, '쌀국수', NOW(), NOW()),
                                                                   (20, '소시지', NOW(), NOW()),
                                                                   (20, '치즈', NOW(), NOW()),
                                                                   (20, '와플', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (225, '한국을 대표하는 발효 음식은?'),
                                                     (226, '생선을 얇게 썰어 만든 일본 음식은?'),
                                                     (227, '이탈리아에서 유래된 빵 위에 토핑을 얹은 음식은?'),
                                                     (228, '멕시코의 전통 음식으로 토르티야를 사용하는 것은?'),
                                                     (229, '스페인의 해산물 볶음밥은?'),
                                                     (230, '프랑스의 바삭한 빵은?'),
                                                     (231, '중국식 찐만두 요리는?'),
                                                     (232, '미국의 대표적인 패스트푸드는?'),
                                                     (233, '인도의 향신료 강한 국물 요리는?'),
                                                     (234, '태국의 매운 해산물 수프는?'),
                                                     (235, '베트남의 국수 요리는?'),
                                                     (236, '독일의 대표적인 육가공 음식은?'),
                                                     (237, '유럽의 대표 유제품은?'),
                                                     (238, '벨기에의 디저트 빵은?');


-- 퀴즈 21: 영어 단어 뜻 맞히기 퀴즈 (문제 20개)
INSERT INTO question (quiz_id, answer, created_at, updated_at) VALUES
                                                                   (21, '사과', NOW(), NOW()),
                                                                   (21, '바나나', NOW(), NOW()),
                                                                   (21, '고양이', NOW(), NOW()),
                                                                   (21, '개', NOW(), NOW()),
                                                                   (21, '행복한', NOW(), NOW()),
                                                                   (21, '슬픈', NOW(), NOW()),
                                                                   (21, '달리다', NOW(), NOW()),
                                                                   (21, '걷다', NOW(), NOW()),
                                                                   (21, '생각하다', NOW(), NOW()),
                                                                   (21, '알다', NOW(), NOW()),
                                                                   (21, '보다', NOW(), NOW()),
                                                                   (21, '듣다', NOW(), NOW()),
                                                                   (21, '말하다', NOW(), NOW()),
                                                                   (21, '쓰다', NOW(), NOW()),
                                                                   (21, '읽다', NOW(), NOW()),
                                                                   (21, '학교', NOW(), NOW()),
                                                                   (21, '의자', NOW(), NOW()),
                                                                   (21, '책상', NOW(), NOW()),
                                                                   (21, '문', NOW(), NOW()),
                                                                   (21, '창문', NOW(), NOW());

INSERT INTO content_question (question_id, content) VALUES
                                                     (239, 'apple의 뜻은?'),
                                                     (240, 'banana의 뜻은?'),
                                                     (241, 'cat의 뜻은?'),
                                                     (242, 'dog의 뜻은?'),
                                                     (243, 'happy의 뜻은?'),
                                                     (244, 'sad의 뜻은?'),
                                                     (245, 'run의 뜻은?'),
                                                     (246, 'walk의 뜻은?'),
                                                     (247, 'think의 뜻은?'),
                                                     (248, 'know의 뜻은?'),
                                                     (249, 'see의 뜻은?'),
                                                     (250, 'hear의 뜻은?'),
                                                     (251, 'say의 뜻은?'),
                                                     (252, 'write의 뜻은?'),
                                                     (253, 'read의 뜻은?'),
                                                     (254, 'school의 뜻은?'),
                                                     (255, 'chair의 뜻은?'),
                                                     (256, 'desk의 뜻은?'),
                                                     (257, 'door의 뜻은?'),
                                                     (258, 'window의 뜻은?');