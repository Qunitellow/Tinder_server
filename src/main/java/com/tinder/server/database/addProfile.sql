TRUNCATE TABLE USERS AND COMMIT;

INSERT INTO USERS (id, gender, username, password, description) VALUES ('1', 'сударь', 'Николай', '1', 'Просто здравствуй, просто как дела?');
INSERT INTO USERS (id, gender, username, password, description) VALUES ('2', 'сударь', 'Сергей', '1', 'Я Серега, го выпьем немного');
INSERT INTO USERS (id, gender, username, password, description) VALUES ('3', 'сударь', 'Инокентий', '1', 'Лучше просто Кеша');
INSERT INTO USERS (id, gender, username, password, description) VALUES ('4', 'сударыня', 'Азора', '1', 'Я вся такая не такая');
INSERT INTO USERS (id, gender, username, password, description) VALUES ('5', 'сударыня', 'Амелия', '1', 'Люблю фотографировать еду и книги, как будто читаю');
INSERT INTO USERS (id, gender, username, password, description) VALUES ('6', 'сударыня', 'Дарлин', '1', 'Ищу с кем поболтать, Эллиот козёл, как всегда игнорит меня');

TRUNCATE TABLE LIKES AND COMMIT;

INSERT INTO LIKES (like_id, like_by, like_to) VALUES ('1', '1', '4');
INSERT INTO LIKES (like_id, like_by, like_to) VALUES ('2', '1', '5');
INSERT INTO LIKES (like_id, like_by, like_to) VALUES ('3', '1', '6');
INSERT INTO LIKES (like_id, like_by, like_to) VALUES ('4', '6', '1');
INSERT INTO LIKES (like_id, like_by, like_to) VALUES ('5', '5', '1');
INSERT INTO LIKES (like_id, like_by, like_to) VALUES ('6', '4', '1');
INSERT INTO LIKES (like_id, like_by, like_to) VALUES ('7', '2', '5');
INSERT INTO LIKES (like_id, like_by, like_to) VALUES ('8', '5', '2');

-- TRUNCATE TABLE DISLIKES AND COMMIT;