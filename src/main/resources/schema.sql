CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(512) NOT NULL,
    CONSTRAINT uq_user_email UNIQUE (email)
);

-- Индекс на email уже есть через UNIQUE (uq_user_email)

CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1024) NOT NULL,
    available BOOLEAN NOT NULL,
    owner_id BIGINT NOT NULL,
    CONSTRAINT fk_item_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Индекс на owner_id, по которому часто запрашиваются items
CREATE INDEX IF NOT EXISTS idx_items_owner_id ON items(owner_id);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    item_id BIGINT NOT NULL,
    booker_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_booking_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_booking_booker FOREIGN KEY (booker_id) REFERENCES users(id)
);

-- Индексы на частые фильтры
CREATE INDEX IF NOT EXISTS idx_bookings_booker_id ON bookings(booker_id);
CREATE INDEX IF NOT EXISTS idx_bookings_item_id ON bookings(item_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    text VARCHAR(1024) NOT NULL,
    item_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created TIMESTAMP NOT NULL,
    CONSTRAINT fk_comment_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(id)
);

-- Добавим индекс на item_id и author_id для ускорения join’ов и выборок
CREATE INDEX IF NOT EXISTS idx_comments_item_id ON comments(item_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comments(author_id);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    description VARCHAR(1024) NOT NULL,
    requester_id BIGINT NOT NULL,
    created TIMESTAMP NOT NULL,
    CONSTRAINT fk_request_requester FOREIGN KEY (requester_id) REFERENCES users(id)
);

-- Индекс на requester_id и created (если сортируешь по дате)
CREATE INDEX IF NOT EXISTS idx_requests_requester_id ON requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_requests_created ON requests(created);
