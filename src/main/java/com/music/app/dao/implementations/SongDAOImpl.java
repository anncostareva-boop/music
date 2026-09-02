package com.music.app.dao.implementations;

import com.music.app.dao.interfaces.ConnectionManager;
import com.music.app.dao.interfaces.ISongDAO;
import com.music.app.entity.Genre;
import com.music.app.entity.Song;
import com.music.app.exception.DataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class SongDAOImpl implements ISongDAO {

    private final ConnectionManager cm;

    public SongDAOImpl(ConnectionManager cm) {
        this.cm = cm;
    }

    private Song mapSong(ResultSet rs) throws SQLException {
        Song song = new Song();

        try {
            song.setSongID(rs.getLong("song_id"));
        } catch (SQLException e) {
            song.setSongID(rs.getLong("id"));
        }

        try {
            song.setSongName(rs.getString("song_name"));
        } catch (SQLException e) {
            song.setSongName(rs.getString("title"));
        }

        // FIXED: Map the audio file name
        try {
            song.setFileName(rs.getString("file_name"));
        } catch (SQLException ignored) {}

        try {
            long uid = rs.getLong("uploaded_by_user_id");
            if (!rs.wasNull()) {
                song.setUserID(uid);
            }
        } catch (SQLException ignored) {}

        try {
            song.setArtistName(rs.getString("artist_name"));
        } catch (SQLException ignored) {}

        try {
            song.setGenreName(rs.getString("genre_name"));
        } catch (SQLException ignored) {}

        try {
            String art = rs.getString("artwork_filename");
            song.setArtworkFileName(art != null && !art.isBlank() ? art : "default-cover.jpg");
        } catch (SQLException ignored) {
            song.setArtworkFileName("default-cover.jpg");
        }

        try {
            song.setLikes(rs.getInt("likes"));
        } catch (SQLException ignored) {}

        return song;
    }

    @Override
    public void addSong(Song song) {
        String sql = """
            INSERT INTO songs (title, artist_id, uploaded_by_user_id, album_id, genre_id, file_name, artwork_filename)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = cm.getConnection(false, Connection.TRANSACTION_READ_COMMITTED);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, song.getSongName());

            if (song.getArtistId() != null)
                ps.setLong(2, song.getArtistId());
            else
                ps.setNull(2, Types.BIGINT);

            if (song.getUserID() != null)
                ps.setLong(3, song.getUserID());
            else
                ps.setNull(3, Types.BIGINT);

            if (song.getAlbumId() != null)
                ps.setLong(4, song.getAlbumId());
            else
                ps.setNull(4, Types.BIGINT);

            ps.setLong(5, song.getGenreId());
            ps.setString(6, song.getFileName());
            ps.setString(7, song.getArtworkFileName());

            ps.executeUpdate();
            con.commit();

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't add song.", e);
        }
    }

    @Override
    public List<Song> getAllSongs() {
        String sql = """
            SELECT 
                s.id AS song_id,
                s.title AS song_name,
                s.genre_id,
                s.artist_id,
                s.uploaded_by_user_id,
                s.album_id,
                s.file_name,
                s.artwork_filename,
                g.name AS genre_name,
                a.name AS artist_name,
                COUNT(ls.song_id) AS likes
            FROM songs s
            LEFT JOIN genres g ON s.genre_id = g.id
            LEFT JOIN artists a ON s.artist_id = a.id
            LEFT JOIN liked_songs ls ON s.id = ls.song_id
            GROUP BY s.id, s.title, s.genre_id, s.artist_id, s.uploaded_by_user_id, 
                     s.album_id, s.file_name, s.artwork_filename, g.name, a.name
        """;
        List<Song> songs = new ArrayList<>();

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                songs.add(mapSong(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get all songs.", e);
        }
        return songs;
    }

    @Override
    public Song findSongById(Long songId) {
        String sql = """
            SELECT 
                s.id AS song_id,
                s.title AS song_name,
                s.genre_id,
                s.artist_id,
                s.uploaded_by_user_id,
                s.album_id,
                s.file_name,
                s.artwork_filename,
                g.name AS genre_name,
                a.name AS artist_name
            FROM songs s
            LEFT JOIN genres g ON s.genre_id = g.id
            LEFT JOIN artists a ON s.artist_id = a.id
            WHERE s.id = ?
        """;
        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, songId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSong(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get song by id.", e);
        }
        return null;
    }

    @Override
    public List<Song> findSongByTitle(String title) {
        List<Object> params = new ArrayList<>();
        List<Song> songs = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT
                s.id AS song_id,
                s.title AS song_name,
                s.genre_id,
                s.artist_id,
                s.uploaded_by_user_id,
                s.album_id,
                s.file_name,
                s.artwork_filename,
                g.name AS genre_name,
                a.name AS artist_name,
                COUNT(ls.song_id) AS likes
            FROM songs s
            LEFT JOIN genres g ON s.genre_id = g.id
            LEFT JOIN artists a ON s.artist_id = a.id
            LEFT JOIN liked_songs ls ON s.id = ls.song_id
            WHERE 1 = 1
        """);

        if (title != null && !title.isBlank()) {
            sql.append(" AND s.title LIKE ? ");
            params.add("%" + title + "%");
        }

        sql.append("""
            GROUP BY s.id, s.title, s.genre_id, s.artist_id, s.uploaded_by_user_id, 
                     s.album_id, s.file_name, s.artwork_filename, g.name, a.name
        """);

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapSong(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get song by title.", e);
        }

        return songs;
    }

    @Override
    public void deleteSong(Song song) {
        String sql = "DELETE FROM songs WHERE id = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, song.getSongID());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Couldn't delete song.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't delete song by id.", e);
        }
    }

    @Override
    public Set<Song> getLikedSongs(Long userId) {
        String sql = """
            SELECT 
                s.id AS song_id,
                s.title AS song_name,
                s.genre_id,
                s.artist_id,
                s.uploaded_by_user_id,
                s.album_id,
                s.file_name,
                s.artwork_filename,
                g.name AS genre_name,
                a.name AS artist_name,
                COUNT(ls_all.song_id) AS likes
            FROM liked_songs ls_user
            INNER JOIN songs s ON ls_user.song_id = s.id
            LEFT JOIN genres g ON s.genre_id = g.id
            LEFT JOIN artists a ON s.artist_id = a.id
            LEFT JOIN liked_songs ls_all ON s.id = ls_all.song_id
            WHERE ls_user.user_id = ?
            GROUP BY s.id, s.title, s.genre_id, s.artist_id, s.uploaded_by_user_id, 
                     s.album_id, s.file_name, s.artwork_filename, g.name, a.name
        """;

        Set<Song> likedSongs = new HashSet<>();

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    likedSongs.add(mapSong(rs));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get liked songs.", e);
        }

        return likedSongs;
    }

    @Override
    public List<Song> getListeningHistory(Long userId) {
        String sql = """
            SELECT 
                s.id AS song_id,
                s.title AS song_name,
                s.genre_id,
                s.artist_id,
                s.uploaded_by_user_id,
                s.album_id,
                s.file_name,
                s.artwork_filename,
                g.name AS genre_name,
                a.name AS artist_name
            FROM listening_history lh
            INNER JOIN songs s ON lh.song_id = s.id
            LEFT JOIN genres g ON s.genre_id = g.id
            LEFT JOIN artists a ON s.artist_id = a.id
            WHERE lh.user_id = ?
            GROUP BY s.id, s.title, s.genre_id, s.artist_id, s.uploaded_by_user_id, 
                     s.album_id, s.file_name, s.artwork_filename, g.name, a.name
        """;

        List<Song> listeningSongs = new ArrayList<>();

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listeningSongs.add(mapSong(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get listening history.", e);
        }
        return listeningSongs;
    }

    @Override
    public List<Song> findRandomSongsByGenre(List<Genre> favGenres) {
        if (favGenres == null || favGenres.isEmpty()) {
            return new ArrayList<>();
        }

        StringJoiner inClauses = new StringJoiner(",", "(", ")");
        for (int i = 0; i < favGenres.size(); i++) {
            inClauses.add("?");
        }

        String sql = "SELECT * FROM songs s " +
                "INNER JOIN genres g ON s.genre_id = g.id " +
                "WHERE g.name IN " + inClauses +
                " ORDER BY RAND() LIMIT 50";

        List<Song> randomSongs = new ArrayList<>();
        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < favGenres.size(); i++) {
                ps.setString(i + 1, favGenres.get(i).getName());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    randomSongs.add(mapSong(rs));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get random songs by genre.", e);
        }
        return randomSongs;
    }

    @Override
    public List<Song> getSongsByUser(Long userId) {
        String sql = """
            SELECT 
                s.id AS song_id,
                s.title AS song_name,
                s.artist_id,
                s.uploaded_by_user_id,
                s.genre_id,
                s.album_id,
                s.file_name,
                s.artwork_filename,
                COALESCE(a.name, 'Unknown Artist') AS artist_name,
                COALESCE(g.name, 'Unknown') AS genre_name,
                (SELECT COUNT(*) FROM liked_songs ls WHERE ls.song_id = s.id) AS likes
            FROM songs s
            LEFT JOIN artists a ON s.artist_id = a.id
            LEFT JOIN genres g ON s.genre_id = g.id
            WHERE s.uploaded_by_user_id = ?
        """;

        List<Song> createdSongs = new ArrayList<>();

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    createdSongs.add(mapSong(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get songs uploaded by user.", e);
        }
        return createdSongs;
    }

    @Override
    public List<Song> trendingSongs() throws DataAccessException {
        String sql = """
            SELECT 
                s.id AS song_id,
                s.title AS song_name,
                s.genre_id,
                s.artist_id,
                s.uploaded_by_user_id,
                s.album_id,
                s.file_name,
                s.artwork_filename,
                g.name AS genre_name,
                a.name AS artist_name,
                COUNT(DISTINCT lh.id) AS play_count,
                COUNT(DISTINCT ls.user_id) AS likes
            FROM listening_history lh
            INNER JOIN songs s ON lh.song_id = s.id
            LEFT JOIN genres g ON s.genre_id = g.id
            LEFT JOIN artists a ON s.artist_id = a.id
            LEFT JOIN liked_songs ls ON s.id = ls.song_id
            GROUP BY s.id, s.title, s.genre_id, s.artist_id, s.uploaded_by_user_id, 
                     s.album_id, s.file_name, s.artwork_filename, g.name, a.name
            HAVING COUNT(DISTINCT lh.id) > 2
            ORDER BY play_count DESC
        """;

        List<Song> trendingSongs = new ArrayList<>();

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                trendingSongs.add(mapSong(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get trending songs.", e);
        }
        return trendingSongs;
    }

    @Override
    public int getLikeCountBySongId(Long songId) throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM liked_songs WHERE song_id = ?";
        int result = 0;

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, songId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get song likes.", e);
        }

        return result;
    }

    @Override
    public void likeSong(Long songId, Long userId) throws DataAccessException {
        String sql = "INSERT IGNORE INTO liked_songs (user_id, song_id) VALUES (?, ?)";
        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, songId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't like song.", e);
        }
    }

    @Override
    public void unlikeSong(Long songId, Long userId) throws DataAccessException {
        String sql = "DELETE FROM liked_songs WHERE user_id = ? AND song_id = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, songId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Couldn't remove your like.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't remove your like.", e);
        }
    }

    @Override
    public void addToHistory(Long songId, Long userId) throws DataAccessException {
        String sql = "INSERT INTO listening_history (user_id, song_id) VALUES (?, ?)";
        try (Connection con = cm.getConnection(false, Connection.TRANSACTION_READ_COMMITTED)) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, userId);
                ps.setLong(2, songId);

                ps.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't add to history.", e);
        }
    }

    @Override
    public void editSong(Song song) throws DataAccessException {
        String sql = "UPDATE songs SET title = ?, genre_id = ? WHERE id = ?";

        try (Connection con = cm.getConnection(false, Connection.TRANSACTION_READ_COMMITTED);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, song.getSongName());
            ps.setLong(2, song.getGenreId());
            ps.setLong(3, song.getSongID());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DataAccessException("Couldn't edit song.");
            }

            con.commit();
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't edit song.", e);
        }
    }
}