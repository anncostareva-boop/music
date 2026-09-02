package com.music.app.service.interfaces;

import com.music.app.entity.Genre;
import com.music.app.entity.Song;
import com.music.app.exception.DataAccessException;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface SongService {

    void addSong(Song song);

    public void deleteSong(Song song, Long userId) throws DataAccessException, SQLException, AccessDeniedException;

    List<Song> getAllSongs() throws SQLException;

    Song getSongById(Long songId) throws SQLException;

    List<Song> getSongByTitle(String title) throws SQLException;

    Set<Song> getLikedSongs(Long userId) throws SQLException;

    List<Song> getListeningHistory(Long userId) throws SQLException;

    List<Song> findRandomSongsByGenre(Long userId) throws SQLException;

    List<Song> getSongsByUser(Long userId);

    List<Song>  trendingSongs() throws DataAccessException;

    void likeSong(Long songId, Long userId) throws DataAccessException;

    void unlikeSong(Long songId, Long userId) throws DataAccessException;

    void addToHistory(Long songId, Long userId) throws DataAccessException;

    void editSong(Song song, Long userId) throws DataAccessException, SQLException, AccessDeniedException;

    public void deleteSong(Song song) throws DataAccessException, SQLException, AccessDeniedException;
}
