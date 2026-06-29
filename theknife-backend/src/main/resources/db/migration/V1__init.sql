-- TheKnife — initial schema, derived from the application domain model.
-- Mordente Marcello 761730 VA / Luciano Alessio 759956 VA / Nardo Luca 761132 VA / Morosini Luca 760029 VA

CREATE TABLE users (
    username   VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    password   VARCHAR(255) NOT NULL,            -- BCrypt hash
    birth_date DATE,
    city       VARCHAR(255),
    role       VARCHAR(20)  NOT NULL             -- 'CLIENTE' | 'RISTORATORE'
);

CREATE TABLE restaurants (
    id             VARCHAR(64) PRIMARY KEY,       -- legacy hash id for seeded data, UUID for new ones
    name           VARCHAR(512) NOT NULL,
    address        VARCHAR(1024),
    location       VARCHAR(512),
    price          VARCHAR(32),
    cuisine        VARCHAR(512),
    longitude      DOUBLE PRECISION,
    latitude       DOUBLE PRECISION,
    phone          VARCHAR(128),
    michelin_url   TEXT,
    website_url    TEXT,
    award          VARCHAR(255),
    green_star     INTEGER,
    facilities     TEXT,
    description    TEXT,
    owner_username VARCHAR(255) REFERENCES users (username) ON DELETE SET NULL
);

CREATE INDEX idx_restaurants_location ON restaurants (location);
CREATE INDEX idx_restaurants_owner    ON restaurants (owner_username);

CREATE TABLE reviews (
    id            VARCHAR(64) PRIMARY KEY,
    restaurant_id VARCHAR(64)  NOT NULL REFERENCES restaurants (id) ON DELETE CASCADE,
    username      VARCHAR(255) NOT NULL REFERENCES users (username) ON DELETE CASCADE,
    content       TEXT,
    stars         INTEGER,
    answer        TEXT,
    CONSTRAINT uq_review_user_restaurant UNIQUE (restaurant_id, username)
);

CREATE INDEX idx_reviews_restaurant ON reviews (restaurant_id);

CREATE TABLE favorites (
    username      VARCHAR(255) NOT NULL REFERENCES users (username) ON DELETE CASCADE,
    restaurant_id VARCHAR(64)  NOT NULL REFERENCES restaurants (id) ON DELETE CASCADE,
    PRIMARY KEY (username, restaurant_id)
);
