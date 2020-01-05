create table IF NOT EXISTS users(
    email varchar primary key,
    password varchar not null,
    registered bool not null,
    first_name varchar,
    last_name varchar,
    username varchar,
    contact varchar
);

create table IF NOT EXISTS registration_status(
    registration_link varchar primary key,
    email varchar not null unique,
    status varchar not null,
    registration_time bigint not null
);

create table IF NOT EXISTS user_session(
    email varchar not null,
    session_id varchar primary key,
    start_at bigint not null,
    end_at bigint
);
