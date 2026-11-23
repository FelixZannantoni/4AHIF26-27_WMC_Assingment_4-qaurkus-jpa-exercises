INSERT INTO book (id, title, author, year) VALUES (1, 'Dune', 'Herbert', 1965);
INSERT INTO book (id, title, author, year) VALUES (2, 'Solaris', 'Lem', 1961);
-- Hibernate takes ids in blocks of 50 from this sequence: move it past the rows above
ALTER SEQUENCE book_seq RESTART WITH 100;
