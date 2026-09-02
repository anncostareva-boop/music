package com.music.app.dao.implementations;

import com.music.app.dao.interfaces.ConnectionManager;
import com.music.app.dao.interfaces.IArtistDAO;
import com.music.app.entity.Artist;
import com.music.app.exception.DataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class ArtistDAOImpl implements IArtistDAO {

    private final ConnectionManager cm;
    public ArtistDAOImpl(ConnectionManager cm) {
        this.cm = cm;
    }

    private Artist mapArtist(ResultSet rs) throws SQLException {
        return new Artist(
                rs.getLong("artistId"),
                rs.getString("name"),
                rs.getLong("userId")
        );

    }

    @Override
    public Long getArtistIdByUserId(Long userId) {

        String sql = "SELECT id FROM artists WHERE user_id = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getLong("id");
                }

                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't find user by user id.", e);
        }
    }

    @Override
    public Long createArtist(Artist artist) {
        String sql = "insert into artists (name, user_id) values (?, ?)";

        try (Connection con = cm.getConnection(false, Connection.TRANSACTION_READ_COMMITTED);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, artist.getArtistName());
            ps.setLong(2, artist.getUserId());

            ps.executeUpdate();
            con.commit();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new DataAccessException("Couldn't return id for artist.");
                }
            }

            } catch (SQLException e) {
                throw new DataAccessException("Couldn't insert artist.", e);
            }
        }

        @Override
    public Artist getArtistByName(String artistName) {
        String sql = "select * from artists where name = ?";

        try(Connection con = cm.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, artistName);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) {
                    return mapArtist(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't find user by name.", e);
        }
        return null;
        }

}

