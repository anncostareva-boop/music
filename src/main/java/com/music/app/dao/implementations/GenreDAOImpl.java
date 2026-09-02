package com.music.app.dao.implementations;

import com.music.app.dao.interfaces.ConnectionManager;
import com.music.app.dao.interfaces.IGenreDAO;
import com.music.app.entity.Genre;
import com.music.app.exception.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GenreDAOImpl implements IGenreDAO {

    private final ConnectionManager cm;
    public GenreDAOImpl(ConnectionManager cm) {
        this.cm = cm;
    }

    private Genre mapGenre(ResultSet rs) throws SQLException {
        return new Genre(
                rs.getString("name")
        );
    }

    @Override
    public List<Genre> findTopGenresByUserId(Long userId) {

        List<Genre> favGenres = new ArrayList<>();

        String sql = "SELECT genres.name, COUNT(genres.name) AS likes\n" +
                "FROM liked_songs\n" +
                "INNER JOIN songs ON liked_songs.song_id=songs.id\n" +
                "INNER JOIN genres ON songs.genre_id=genres.id\n" +
                "WHERE liked_songs.user_id = ?\n" +
                "GROUP BY genres.name\n" +
                "ORDER BY likes DESC\n" +
                "LIMIT 3\n";

        try(Connection con = cm.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                favGenres.add(mapGenre(rs));
            }

        }catch(SQLException e) {
            throw new DataAccessException("Error finding top genres");
        }
        return favGenres;
    }

    @Override
    public Genre findByName(String name) {

        String sql = """
            SELECT id, name
            FROM genres
            WHERE name = ?
            """;

        try(Connection con = cm.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                Genre genre = new Genre();
                genre.setId(rs.getLong("id"));
                genre.setName(rs.getString("name"));
                return genre;
            }

        } catch(SQLException e) {
            throw new DataAccessException("Error finding genre", e);
        }

        return null;
    }

    @Override
    public Long addGenreId(Genre genre) {
        String sql = "INSERT INTO genres (name) VALUES (?)";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, genre.getName());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new DataAccessException("Error fetching generated genre ID");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error adding genre", e);
        }
    }
}
