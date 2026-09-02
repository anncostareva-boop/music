package com.music.app.dao.implementations;

import com.music.app.dao.interfaces.ConnectionManager;
import com.music.app.dao.interfaces.IAlbumDAO;
import com.music.app.entity.Album;
import com.music.app.entity.Song;
import com.music.app.exception.DataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class AlbumDAOImpl implements IAlbumDAO {

    private final ConnectionManager cm;

    public AlbumDAOImpl(ConnectionManager cm) {
        this.cm = cm;
    }

    private Album mapAlbum(ResultSet rs) throws SQLException {
        Album album = new Album();
        album.setAlbumId(rs.getLong("id"));
        album.setAlbumName(rs.getString("title"));

        Long artistId = rs.getObject("artist_id") != null ? rs.getLong("artist_id") : null;
        album.setArtistId(artistId);

        Long userId = rs.getObject("user_id") != null ? rs.getLong("user_id") : null;
        album.setUserId(userId);

        String artwork = rs.getString("artwork_filename");
        album.setArtworkFileName(artwork != null && !artwork.isBlank() ? artwork : "default-cover.jpg");

        try {
            album.setArtistName(rs.getString("artist_name"));
        } catch (SQLException ignored) {}

        return album;
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
    public List<Album> getAllAlbums() {
        String sql = """
            SELECT 
                al.id, 
                al.title, 
                al.artist_id, 
                al.user_id,
                al.artwork_filename, 
                ar.name AS artist_name 
            FROM albums al
            LEFT JOIN artists ar ON al.artist_id = ar.id
        """;
        List<Album> albums = new ArrayList<>();

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                albums.add(mapAlbum(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error while fetching albums from DB", e);
        }
        return albums;
    }

    @Override
    public void addAlbum(Album album) {
        String sql = "INSERT INTO albums (title, artist_id, user_id, artwork_filename) VALUES (?, ?, ?, ?)";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, album.getAlbumName());
            ps.setObject(2, album.getArtistId());
            ps.setObject(3, album.getUserId());
            ps.setString(4, album.getArtworkFileName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error while adding an album", e);
        }
    }

    @Override
    public void deleteAlbum(Album album) {
        String sqlUpdateSongs = "UPDATE songs SET album_id = NULL WHERE album_id = ?";
        String sqlDeleteAlbum = "DELETE FROM albums WHERE id = ?";

        Connection con = null;

        try {
            con = cm.getConnection(false, Connection.TRANSACTION_READ_COMMITTED);

            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdateSongs);
                 PreparedStatement psDelete = con.prepareStatement(sqlDeleteAlbum)) {

                psUpdate.setLong(1, album.getAlbumId());
                psUpdate.executeUpdate();

                psDelete.setLong(1, album.getAlbumId());
                int rowsAffected = psDelete.executeUpdate();

                if (rowsAffected == 0) {
                    throw new DataAccessException("No album with such ID exists.");
                }

                con.commit();
            }

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
            throw new DataAccessException("Error while deleting an album.", e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Album getAlbumById(long id) {
        String sql = "SELECT * FROM albums WHERE id = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAlbum(rs);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error while fetching an album with such ID.", e);
        }
        return null;
    }

    @Override
    public Album getAlbumByName(String name) {
        String sql = "SELECT * FROM albums WHERE title = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAlbum(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error while fetching an album name.", e);
        }

        return null;
    }

    @Override
    public void addSongToAlbum(Long id, Long songId) {
        String sql = "UPDATE songs SET album_id = ? WHERE id = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, songId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Couldn't add song to the album (song not found).");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error while adding a song to an album.", e);
        }
    }

    @Override
    public void deleteSongFromAlbum(Long id, Long songId) {
        String sql = "UPDATE songs SET album_id = NULL WHERE album_id = ? AND id = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, songId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Couldn't delete song from the album.");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error while deleting a song from an album.", e);
        }
    }

    @Override
    public List<Song> getAllSongs(Long id) {
        List<Song> albumSong = new ArrayList<>();

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
            WHERE s.album_id = ?
            GROUP BY s.id, s.title, s.genre_id, s.artist_id, s.uploaded_by_user_id, 
                     s.album_id, s.file_name, s.artwork_filename, g.name, a.name
        """;

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    albumSong.add(mapSong(rs));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error while fetching songs from an album.", e);
        }
        return albumSong;
    }

    @Override
    public Set<Album> getAllAlbumsByArtistId(Long userId) {
        Set<Album> albums = new HashSet<>();

        String sql = """
        SELECT 
            al.id, 
            al.title, 
            al.artist_id, 
            al.user_id,
            al.artwork_filename, 
            ar.name AS artist_name 
        FROM albums al
        LEFT JOIN artists ar ON al.artist_id = ar.id
        WHERE al.user_id = ?
    """;

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    albums.add(mapAlbum(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error while fetching user albums.", e);
        }
        return albums;
    }
}