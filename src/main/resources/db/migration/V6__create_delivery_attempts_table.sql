CREATE TABLE delivery_attempts (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    attempt_timestamp TIMESTAMP NOT NULL,
    success BOOLEAN NOT NULL,
    response_message TEXT,
    CONSTRAINT fk_delivery
        FOREIGN KEY(delivery_id)
            REFERENCES deliveries(id)
);
