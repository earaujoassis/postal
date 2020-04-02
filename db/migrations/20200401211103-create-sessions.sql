CREATE TABLE IF NOT EXISTS user_sessions (
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id                  SERIAL NOT NULL PRIMARY KEY,
    uuid                VARCHAR(36) UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    user_id             INTEGER REFERENCES users(id) NOT NULL,
    access_token        VARCHAR(128) UNIQUE NOT NULL,
    expires_in          INTEGER NOT NULL,
    refresh_token       VARCHAR(128) NOT NULL,
    scope               VARCHAR(64) NOT NULL,
    token_type          VARCHAR(64) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS user_sessions_uuid ON user_sessions (uuid);
CREATE UNIQUE INDEX IF NOT EXISTS user_sessions_access_token ON user_sessions (access_token);
