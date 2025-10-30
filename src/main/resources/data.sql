-- MPA (фиксированные id)
INSERT INTO mpa (id, name) VALUES
                               (1, 'G'),
                               (2, 'PG'),
                               (3, 'PG-13'),
                               (4, 'R'),
                               (5, 'NC-17');

-- Жанры
INSERT INTO genres (id, name) VALUES
                                  (1, 'COMEDY'),
                                  (2, 'DRAMA'),
                                  (3, 'CARTOON'),
                                  (4, 'THRILLER'),
                                  (5, 'DOCUMENTARY'),
                                  (6, 'ACTION');

-- Статусы дружбы
INSERT INTO friend_status (id, name) VALUES
                                         (1, 'CONFIRMED');
