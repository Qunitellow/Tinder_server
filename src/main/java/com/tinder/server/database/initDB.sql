-- CREATE TABLE IF NOT EXISTS USER
-- (
--     ID       BIGINT          NOT NULL PRIMARY KEY,
--     LOGIN    VARCHAR(255) NOT NULL,
--     PASSWORD VARCHAR(255) NOT NULL
-- );

CREATE TABLE IF NOT EXISTS USERS
(
    id          INT          NOT NULL PRIMARY KEY,
    gender      VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    nickname    VARCHAR(255) NOT NULL,
    description VARCHAR(255)
);
