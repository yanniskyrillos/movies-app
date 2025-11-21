create table if not exists users (
                       id serial primary key,
                       username varchar(300),
                       password varchar(300)
);

create table if not exists movies (
    id serial primary key,
    title varchar(100),
    description varchar(300),
    user_id bigint references users(id),
    username varchar(300),
    publication_date timestamp,
    likes int,
    dislikes int
);

create table if not exists opinions (
    user_id bigint,
    movie_id bigint,
    liked boolean,
    constraint opinion_id unique (user_id, movie_id)
);
