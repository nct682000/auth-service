CREATE TABLE "user"
(
    id                     UUID PRIMARY KEY,
    username               VARCHAR(100) UNIQUE NOT NULL,
    password               VARCHAR(255)        NOT NULL,
    email                  VARCHAR(255),
    status                 VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    token_version          INT                 NOT NULL DEFAULT 1,
    account_expired_at     TIMESTAMP NULL,
    credentials_expired_at TIMESTAMP NULL,
    created_at             TIMESTAMP,
    updated_at             TIMESTAMP,
    deleted_at             TIMESTAMP
);

CREATE TABLE role
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE permission
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);