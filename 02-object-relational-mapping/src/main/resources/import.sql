INSERT INTO author (name) VALUES ('Herbert'), ('Lem');
INSERT INTO book (id, title, author_id, published_year, genre, isbn) VALUES (1, 'Dune', 1, 1965, 'SCIENCE_FICTION', '9780441013593');
INSERT INTO book (id, title, author_id, published_year, genre, isbn) VALUES (2, 'Solaris', 2, 1961, 'SCIENCE_FICTION', '9780156027601');
-- Hibernate takes ids in blocks of 50 from this sequence: move it past the rows above
ALTER SEQUENCE book_seq RESTART WITH 100;
INSERT INTO member (id, name) VALUES ('00000000-0000-0000-0000-000000000001', 'Ada'), ('00000000-0000-0000-0000-000000000002', 'Alan');
