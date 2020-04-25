TRUNCATE TABLE USERS AND COMMIT;

INSERT INTO USERS (id, gender, username, password, description) VALUES ('1', 'сударь', 'Николай', '1', 'Просто здравствуй, просто как дела?');
INSERT INTO USERS (id, gender, username, password, description) VALUES ('2', 'сударь', 'Сергей', '1', 'Я Серега, го выпьем немного');
INSERT INTO USERS (id, gender, username, password, description) VALUES ('3', 'сударь', 'Инокентий', '1', 'Лучше просто Кеша');
INSERT INTO USERS (id, gender, username, password, description) VALUES ('4', 'сударыня', 'Азора', '1', 'Я вся такая не такая');
INSERT INTO USERS (id, gender, username, password, description) VALUES ('5', 'сударыня', 'Амелия', '1', 'Люблю фотографировать еду и книги, как будто читаю');
INSERT INTO USERS (id, gender, username, password, description) VALUES ('6', 'сударыня', 'Дарлин', '1', 'Ищу с кем поболтать, Эллиот козёл, как всегда игнорит меня');

TRUNCATE TABLE LIKES AND COMMIT;

INSERT INTO LIKES (like_id, like_by, like_to) VALUES ('1', '1', '6');
INSERT INTO LIKES (like_id, like_by, like_to) VALUES ('2', '6', '1');

