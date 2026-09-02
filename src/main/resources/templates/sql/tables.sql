-- ==========================
-- USERS
-- ==========================
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       phone VARCHAR(50) NOT NULL,
                       role VARCHAR(20) NOT NULL
);

-- ==========================
-- ARTISTS
-- ==========================
CREATE TABLE artists (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         name VARCHAR(100) UNIQUE NOT NULL,
                         user_id BIGINT NOT NULL UNIQUE,

                             FOREIGN KEY (user_id)
    REFERENCES users(id)
);

-- ==========================
-- ALBUMS
-- ==========================
CREATE TABLE albums (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        title VARCHAR(100) NOT NULL,
                        artist_id BIGINT NOT NULL,
                        release_year INT,
                        artwork_filename BLOB,
                        user_id BIGINT,


                        FOREIGN KEY (artist_id)
                            REFERENCES artists(id),

                        FOREIGN KEY (user_id)
                            REFERENCES users(id)
);

-- ==========================
-- GENRES
-- ==========================
CREATE TABLE genres (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(50) UNIQUE NOT NULL
);

-- ==========================
-- SONGS
-- ==========================
CREATE TABLE songs (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       title VARCHAR(100) NOT NULL,
                       artist_id BIGINT NOT NULL,
                       uploaded_by_user_id BIGINT NOT NULL,
                       album_id BIGINT,
                       genre_id BIGINT,
                       file_name BLOB NOT NULL,
                       artwork_filename BLOB,

                       FOREIGN KEY (artist_id)
                           REFERENCES artists(id),

                       FOREIGN KEY (uploaded_by_user_id)
                           REFERENCES users(id),

                       FOREIGN KEY (album_id)
                           REFERENCES albums(id),

                       FOREIGN KEY (genre_id)
                           REFERENCES genres(id)
);

-- ==========================
-- PLAYLISTS
-- ==========================
CREATE TABLE playlists (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           name VARCHAR(100) NOT NULL,
                           user_id BIGINT NOT NULL,
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           artwork_filename BLOB,

                           FOREIGN KEY (user_id)
                               REFERENCES users(id)
);

-- ==========================
-- PLAYLIST SONGS
-- ==========================
CREATE TABLE playlist_songs (
                                playlist_id BIGINT NOT NULL,
                                song_id BIGINT NOT NULL,

                                PRIMARY KEY (playlist_id, song_id),

                                FOREIGN KEY (playlist_id)
                                    REFERENCES playlists(id)
                                    ON DELETE CASCADE,

                                FOREIGN KEY (song_id)
                                    REFERENCES songs(id)
                                    ON DELETE CASCADE
);

-- ==========================
-- LIKED SONGS
-- ==========================
CREATE TABLE liked_songs (
                             user_id BIGINT NOT NULL,
                             song_id BIGINT NOT NULL,
                             liked_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                             PRIMARY KEY (user_id, song_id),

                             FOREIGN KEY (user_id)
                                 REFERENCES users(id)
                                 ON DELETE CASCADE,

                             FOREIGN KEY (song_id)
                                 REFERENCES songs(id)
                                 ON DELETE CASCADE
);

-- ==========================
-- LISTENING HISTORY
-- ==========================
CREATE TABLE listening_history (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   user_id BIGINT NOT NULL,
                                   song_id BIGINT NOT NULL,

                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE,

                                   FOREIGN KEY (song_id)
                                       REFERENCES songs(id)
                                       ON DELETE CASCADE
);

-- ==========================
-- LIKED PLAYLISTS
-- ==========================
CREATE TABLE liked_playlists (
                                 user_id BIGINT NOT NULL,
                                 playlist_id BIGINT NOT NULL,
                                 liked_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                                 PRIMARY KEY (user_id, playlist_id),

                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
                                     ON DELETE CASCADE,

                                 FOREIGN KEY (playlist_id)
                                     REFERENCES playlists(id)
                                     ON DELETE CASCADE
);

CREATE TABLE album_songs (
                             album_id BIGINT NOT NULL,
                             song_id BIGINT NOT NULL,

                             PRIMARY KEY (album_id, song_id),

                             FOREIGN KEY (album_id)
                                 REFERENCES albums(id)
                                 ON DELETE CASCADE,

                             FOREIGN KEY (song_id)
                                 REFERENCES songs(id)
                                 ON DELETE CASCADE
);
