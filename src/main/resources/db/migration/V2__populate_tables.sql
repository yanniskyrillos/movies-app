insert into users (username, password)
values
    ('user1', 'pass1'),
    ('user2', 'pass2');
insert into movies (title, description, user_id, publication_date, likes, dislikes)
values ('movie1', 'desc1 this is a movie description long enough to see if the length creates something bad in css', 1,
        '2020-01-01', 1, 0),
       ('movie2', 'desc2', 2, '2020-01-01', 3, 1),
       ('movie3', 'desc3', 1, '2020-01-01', 1, 1),
       ('movie4', 'desc4', 2, '2020-01-01', 4, 2),
       ('movie5', 'desc5', 1, '2020-01-01', 5, 0),
       ('movie6', 'desc6', 2, '2020-01-01', 6, 1);
