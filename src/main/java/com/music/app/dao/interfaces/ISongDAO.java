package com.music.app.dao.interfaces;

import com.music.app.entity.Genre;
import com.music.app.entity.Song;
import com.music.app.entity.User;
import com.music.app.exception.DataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface ISongDAO {

    void addSong(Song song) throws DataAccessException;
    List<Song> getAllSongs() throws SQLException;
    Song findSongById(Long SongId) throws SQLException;
    List<Song> findSongByTitle(String title) throws SQLException;
    public void deleteSong(Song song) throws DataAccessException;
    Set<Song> getLikedSongs(Long userId) throws SQLException;
    List<Song> getListeningHistory(Long userId) throws SQLException;
    List<Song> findRandomSongsByGenre(List<Genre> favGenres) throws SQLException;
    List<Song> getSongsByUser(Long userId);
    List<Song>  trendingSongs() throws DataAccessException;
    int getLikeCountBySongId(Long songId) throws DataAccessException;
    void likeSong(Long songId, Long userId) throws DataAccessException;
    void unlikeSong(Long songId, Long userId) throws DataAccessException;
    void addToHistory(Long songId, Long userId) throws DataAccessException;
    void editSong(Song song) throws DataAccessException;
}
