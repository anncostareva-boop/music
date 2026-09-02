package com.music.app.service.implementations;

import com.music.app.dao.interfaces.IGenreDAO;
import com.music.app.dao.interfaces.ISongDAO;
import com.music.app.entity.Genre;
import com.music.app.entity.Song;
import com.music.app.exception.DataAccessException;
import com.music.app.service.interfaces.SongService;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class SongServiceImpl implements SongService {

    private final ISongDAO songDAO;
    private final IGenreDAO genreDAO;

    public SongServiceImpl(ISongDAO songDAO,  IGenreDAO genreDAO) {
        this.songDAO = songDAO;
        this.genreDAO = genreDAO;
    }

    @Override
    public void addSong(Song song) {
        songDAO.addSong(song);
    }

    @Override
    public List<Song> getAllSongs() throws SQLException {

        List<Song> songs = songDAO.getAllSongs();

        for (Song song : songs) {
            song.setSongLikesAmount(
                    songDAO.getLikeCountBySongId(song.getSongID())
            );
        }

        return songs;
    }

    @Override
    public Song getSongById(Long songId) throws SQLException {
        return songDAO.findSongById(songId);
    }

    @Override
    public List<Song> getSongByTitle(String title) throws SQLException {
        return songDAO.findSongByTitle(title);
    }

    @Override
    public Set<Song> getLikedSongs(Long userId) throws SQLException {
        return songDAO.getLikedSongs(userId);
    }

    @Override
    public List<Song> getListeningHistory(Long userId) throws SQLException {
        return songDAO.getListeningHistory(userId);
    }

    @Override
    public void deleteSong(Song song, Long userId) throws SQLException, AccessDeniedException {
        if (song == null || song.getSongID() == null) {
            throw new DataAccessException("Invalid song data.");
        }

        Song existingSong = songDAO.findSongById(song.getSongID());
        if (existingSong == null) {
            throw new DataAccessException("Song not found");
        }

        if (!Objects.equals(existingSong.getUserID(), userId)) {
            throw new AccessDeniedException("You can delete only your own songs.");
        }

        songDAO.deleteSong(existingSong);
    }

    @Override
    public void deleteSong(Song song) throws SQLException {
        if (song == null || song.getSongID() == null) {
            throw new DataAccessException("Invalid song data.");
        }

        Song existingSong = songDAO.findSongById(song.getSongID());
        if (existingSong == null) {
            throw new DataAccessException("Song not found");
        }

        songDAO.deleteSong(existingSong);
    }

    @Override
    public List<Song> findRandomSongsByGenre(Long userId) throws SQLException {
        List<Genre> favGenres = genreDAO.findTopGenresByUserId(userId);
        return songDAO.findRandomSongsByGenre(favGenres);
    }

    @Override
    public List<Song> getSongsByUser(Long userId) {
        return songDAO.getSongsByUser(userId);
    }

    @Override
    public  List<Song>  trendingSongs() {
        return songDAO.trendingSongs();
    }

    @Override
    public void likeSong(Long songId, Long userId) throws DataAccessException {
        songDAO.likeSong(songId,userId);
    }

    @Override
    public void unlikeSong(Long songId, Long userId) throws DataAccessException {
        songDAO.unlikeSong(songId,userId);
    }

    @Override
    public void addToHistory(Long songId, Long userId) throws DataAccessException {
        songDAO.addToHistory(songId,userId);
    }

    @Override
    public void editSong(Song song, Long userId) throws DataAccessException, SQLException, AccessDeniedException {

        Song existingSong = songDAO.findSongById(song.getSongID());

        if (existingSong == null) {
            throw new DataAccessException("Song not found");
        }

        if (!existingSong.getUserID().equals(userId)) {
            throw new AccessDeniedException("You can edit only your own songs.");
        }
        songDAO.editSong(song);
    }

}