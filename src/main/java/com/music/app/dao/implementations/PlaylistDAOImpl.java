package com.music.app.dao.implementations;

import com.music.app.dao.interfaces.ConnectionManager;
import com.music.app.dao.interfaces.IAlbumDAO;
import com.music.app.dao.interfaces.IPlaylistDao;
import com.music.app.entity.Playlist;
import com.music.app.entity.Song;
import com.music.app.exception.DataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PlaylistDAOImpl implements IPlaylistDao {

    private final ConnectionManager cm;

    public PlaylistDAOImpl(ConnectionManager cm) {
        this.cm = cm;
    }

    private Playlist mapPlaylist(ResultSet rs) throws SQLException {
        Playlist playlist = new Playlist();

        // Safe ID mapping (handles 'id' or 'playlist_id')
        if (hasColumn(rs, "id")) {
            playlist.setPlaylistId(rs.getLong("id"));
        } else if (hasColumn(rs, "playlist_id")) {
            playlist.setPlaylistId(rs.getLong("playlist_id"));
        }

        // Playlist Name
        if (hasColumn(rs, "name")) {
            playlist.setPlaylistName(rs.getString("name"));
        } else if (hasColumn(rs, "playlist_name")) {
            playlist.setPlaylistName(rs.getString("playlist_name"));
        }

        // User ID (Set both IDs if needed by your model)
        if (hasColumn(rs, "user_id")) {
            Long userId = rs.getLong("user_id");
            playlist.setUserId(userId);
            playlist.setUploadedByUserId(userId);
        }

        // Cover Artwork
        if (hasColumn(rs, "artwork_filename")) {
            playlist.setArtworkFileName(rs.getString("artwork_filename"));
        }

        // Author
        if (hasColumn(rs, "playlist_author")) {
            playlist.setPlaylistAuthor(rs.getString("playlist_author"));
        }

        // Likes count
        if (hasColumn(rs, "likes")) {
            playlist.setPlaylistLikesAmount(rs.getInt("likes"));
        }

        return playlist;
    }

    private Song mapSong(ResultSet rs) throws SQLException {
        // Safe column checks
        Long songId = hasColumn(rs, "song_id") ? rs.getLong("song_id") : rs.getLong("id");
        String title = hasColumn(rs, "song_name") ? rs.getString("song_name") : rs.getString("title");
        int likes = hasColumn(rs, "likes") ? rs.getInt("likes") : 0; // FIXED

        String genreName = hasColumn(rs, "genre_name") ? rs.getString("genre_name") : null;
        String artistName = hasColumn(rs, "artist_name") ? rs.getString("artist_name") : null;
        String artworkFileName = hasColumn(rs, "artwork_filename") && rs.getString("artwork_filename") != null
                ? rs.getString("artwork_filename") : "default-cover.jpg";

        return new Song(
                songId,
                title,
                hasColumn(rs, "artist_id") ? rs.getLong("artist_id") : null,
                hasColumn(rs, "uploaded_by_user_id") ? rs.getLong("uploaded_by_user_id") : null,
                rs.getObject("album_id") != null ? rs.getLong("album_id") : null,
                hasColumn(rs, "genre_id") ? rs.getLong("genre_id") : null,
                hasColumn(rs, "file_name") ? rs.getString("file_name") : null,
                artworkFileName,
                genreName,
                artistName,
                likes
        );
    }

    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<Playlist> getPlaylists() {
        List<Playlist> playlists = new ArrayList<>();

        String sql = """
        SELECT 
            p.id,
            p.name,
            p.user_id,
            p.artwork_filename,
            u.username AS playlist_author,
            COUNT(lp.playlist_id) AS likes
        FROM playlists p
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN liked_playlists lp ON p.id = lp.playlist_id
        GROUP BY p.id, p.name, p.user_id, p.artwork_filename, u.username
        """;

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                playlists.add(mapPlaylist(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return playlists;
    }

    @Override
    public void addPlaylist(Playlist playlist) {

        String sql = "INSERT INTO playlists (name, user_id, uploaded_by_user_id, artwork_filename) VALUES ( ?, ?, ?, ?)";

        try(Connection con = cm.getConnection(false, Connection.TRANSACTION_READ_COMMITTED);
        PreparedStatement ps = con.prepareStatement(sql);) {

            ps.setString(1, playlist.getPlaylistName());
            ps.setLong(2, playlist.getUserId());
            ps.setLong(3, playlist.getUploadedByUserId());
            ps.setString(4, playlist.getArtworkFileName());
            ps.executeUpdate();

            con.commit();

        }catch(SQLException e ) {
            throw new DataAccessException("Error adding playlist", e);
        }
    }

    @Override
    public void deletePlaylist(Playlist playlist) {

        String sql = "DELETE FROM playlists WHERE id = ?";

        try(Connection con = cm.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);)
        {
            ps.setLong(1, playlist.getPlaylistId());
            int rowsaffected = ps.executeUpdate();

            if(rowsaffected == 0) {
                throw new DataAccessException("Delete playlist failed");
            }

        }catch (SQLException e) {
        throw new DataAccessException("Error deleting playlist", e);}
    }

    @Override
    public List<Playlist> getLikedPlaylists(Long userId) {

        List<Playlist> likedPlaylists = new ArrayList<>();

        String sql = """
        SELECT 
            p.id,
            p.name,
            p.user_id,
            p.artwork_filename,
            u.username AS playlist_author,
            COUNT(lp_count.playlist_id) AS likes
        FROM liked_playlists lp
        INNER JOIN playlists p ON lp.playlist_id = p.id
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN liked_playlists lp_count ON p.id = lp_count.playlist_id
        WHERE lp.user_id = ?
        GROUP BY p.id, p.name, p.user_id, p.artwork_filename, u.username
        """;

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    likedPlaylists.add(mapPlaylist(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return likedPlaylists;
    }

    @Override
    public List<Playlist> getPlaylistsByUser(Long userId){

        String sql = "SELECT * FROM playlists WHERE uploaded_by_user_id = ?";

        List<Playlist> createdPlaylists = new ArrayList<>();

        try(Connection con = cm.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    createdPlaylists.add(mapPlaylist(rs));
                }
            }
        } catch(SQLException e) {
            throw new DataAccessException("Couldn't get playlists uploaded by user.", e);
        }
        return createdPlaylists;
    }

    @Override
    public int getLikeCountByPlaylistId(Long playlistId) {

        String sql = "SELECT COUNT(*) FROM liked_playlists WHERE playlist_id = ?";

int amount = 0;
        try(Connection con = cm.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setLong(1, playlistId);

            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
amount = rs.getInt(1);
                }
            }
        } catch(SQLException e) {
            throw new DataAccessException("Couldn't get like count by playlist.", e);
        }
        return amount;
    }

    @Override
    public void likePlaylist(Long playlistId, Long userId) {

        String sql = "INSERT INTO liked_playlists (playlist_id, user_id) VALUES (?, ?)";

        try(Connection con = cm.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setLong(1, playlistId);
            ps.setLong(2, userId);

            ps.executeUpdate();
        } catch(SQLException e) {
            throw new DataAccessException("Couldn't like playlist.", e);
        }
    }

    @Override
    public void unlikePlaylist(Long playlistId, Long userId) {

        String sql = "DELETE FROM liked_playlists WHERE playlist_id = ? AND user_id = ?";

        int rowsAffected = 0 ;
        try(Connection con = cm.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setLong(1, playlistId);
            ps.setLong(2, userId);

            rowsAffected = ps.executeUpdate();
            if(rowsAffected == 0) {
                throw new DataAccessException("Couldn't unlike playlist.");
            }

        } catch(SQLException e) {
            throw new DataAccessException("Couldn't unlike playlist.", e);
        }
    }

    @Override
    public List<Song> showSongs(Long playlistId) {
        List<Song> songs = new ArrayList<>();

        String sql = "SELECT   s.id AS song_id,\n" +
                "        s.title AS song_name,\n" +
                "        s.genre_id,\n" +
                "        s.artist_id,\n" +
                "        s.uploaded_by_user_id,\n" +
                "        s.album_id,\n" +
                "        s.file_name,\n" +
                "        s.artwork_filename,\n" +
                "        g.name AS genre_name,\n" +
                "        a.name AS artist_name,\n" +
                "        COUNT(ls.song_id) AS likes\n" +
                "    FROM songs s\n" +
                "    LEFT JOIN genres g ON s.genre_id = g.id\n" +
                "    LEFT JOIN artists a ON s.artist_id = a.id\n" +
                "    LEFT JOIN liked_songs ls ON s.id = ls.song_id" +
                "    JOIN playlist_songs ps ON ps.song_id = s.id" +
                "    WHERE ps.playlist_id = ?" +
                "    GROUP BY s.id, s.title, s.genre_id, s.artist_id, s.uploaded_by_user_id, s.album_id, s.file_name, s.artwork_filename, g.name, a.name";

        try(Connection con = cm.getConnection();
        PreparedStatement ps = con.prepareStatement(sql) ) {

            ps.setLong(1, playlistId);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                songs.add(mapSong(rs));
            }
        } catch(SQLException e) {
            throw new DataAccessException("Couldn't show songs.", e);
        }
            return songs;
    }

    @Override
    public Playlist getPlaylistbyId(Long playlistId) {
        String sql = """
        SELECT 
            p.id,
            p.name,
            p.user_id,
            p.artwork_filename,
            u.username AS playlist_author,
            COUNT(lp.playlist_id) AS likes
        FROM playlists p
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN liked_playlists lp ON p.id = lp.playlist_id
        WHERE p.id = ?
        GROUP BY p.id, p.name, p.user_id, p.artwork_filename, u.username
        """;

        try(Connection con = cm.getConnection();
            PreparedStatement ps = con.prepareStatement(sql) ) {

            ps.setLong(1, playlistId);
            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()) {
                    return mapPlaylist(rs);
                }}
        }catch (SQLException e) {
            throw new DataAccessException("Couldn't get playlist.", e);
        }
        return null;
    }

    @Override
    public void addSongToPlaylist(Long playlistId, Long songId) {
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)";

        try(Connection con = cm.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, playlistId);
            ps.setLong(2, songId);

            int rowsAffected = ps.executeUpdate();

            if(rowsAffected == 0) {
                throw new DataAccessException("Couldn't add song to playlist.");
            }
        } catch(SQLException e) {
            throw new DataAccessException("Couldn't add song to playlist.", e);
        }
    }

    @Override
    public Song getSongbyId(Long songId, Long playlistId) {
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
        FROM playlist_songs ps
        INNER JOIN songs s ON ps.song_id = s.id
        LEFT JOIN genres g ON s.genre_id = g.id
        LEFT JOIN artists a ON s.artist_id = a.id
        WHERE ps.playlist_id = ? AND ps.song_id = ?
        """;

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, playlistId);
            ps.setLong(2, songId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSong(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't check song in playlist.", e);
        }
        return null;
}

@Override
    public void deleteSongFromPlaylist(Long playlistId, Long songId) {
        String sql = "DELETE from playlist_songs where playlist_id = ? and song_id = ?";

    try (Connection con = cm.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setLong(1, playlistId);
        ps.setLong(2, songId);
        int rowsAffected = ps.executeUpdate();
        if(rowsAffected == 0) {
            throw new DataAccessException("Couldn't delete song from playlist.");
    }
    }catch(SQLException e) {
        throw new DataAccessException("Couldn't delete song from playlist.", e);
    }
}
    }


