create sequence client_id_seq;

create TABLE client (
    id BIGINT PRIMARY KEY DEFAULT nextval('client_id_seq'),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    rank VARCHAR(255) NOT NULL,
    details JSONB
);