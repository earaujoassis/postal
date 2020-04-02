CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    id                  SERIAL NOT NULL PRIMARY KEY,
    uuid                VARCHAR(36) UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    external_id         VARCHAR(64) UNIQUE NOT NULL,
    full_name           VARCHAR(255),
    metadata            JSON
);

CREATE UNIQUE INDEX IF NOT EXISTS users_uuid ON users (uuid);
create UNIQUE INDEX IF NOT EXISTS users_external_id ON users (external_id);

CREATE TABLE IF NOT EXISTS emails (
    id                  SERIAL NOT NULL PRIMARY KEY,
    uuid                VARCHAR(36) UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    public_id           VARCHAR(64) UNIQUE NOT NULL,
    user_id             INTEGER REFERENCES users(id),
    bucket_key          VARCHAR(64) UNIQUE NOT NULL,
    sent_at             VARCHAR(25) NOT NULL,
    subject             TEXT NOT NULL,
    from_email          VARCHAR(255) NOT NULL,
    from_personal       VARCHAR(255),
    to_email            VARCHAR(255) NOT NULL,
    bcc_email           VARCHAR(255),
    cc_email            VARCHAR(255),
    reply_to            VARCHAR(255),
    body_plain          TEXT,
    body_html           TEXT,
    metadata            JSON
);

CREATE UNIQUE INDEX IF NOT EXISTS emails_uuid ON emails (uuid);
CREATE UNIQUE INDEX IF NOT EXISTS emails_public_id ON emails (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS emails_bucket_key ON emails (bucket_key);
