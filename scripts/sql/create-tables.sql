create table IF NOT EXISTS users(
    email varchar primary key,
    password varchar not null,
    first_name varchar,
    last_name varchar,
    username varchar,
    contact varchar
);

create table IF NOT EXISTS registration_status(
    registration_link varchar primary key,
    email varchar not null unique,
    status varchar not null,
    registration_time date not null
);
