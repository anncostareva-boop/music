package com.music.app.service.interfaces;

import com.music.app.entity.Playlist;
import com.music.app.entity.Song;
import com.music.app.exception.DataAccessException;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.List;

public interface PlaylistService {

    List<Playlist> getPlaylists();

    void addPlaylist(Playlist playlist);

    void deletePlaylist(Playlist playlist);

    public List<Playlist> getLikedPlaylists(Long userId);

    public List<Playlist> getPlaylistsByUser(Long userId);

    List<Song> showSongs(Long playlistId) throws DataAccessException;

    Playlist getPlaylistbyId(Long playlistId) throws DataAccessException;

    void addSongToPlaylist(Long playlistId, Long songId, Long userId) throws DataAccessException, SQLException, AccessDeniedException;

    void deleteSongFromPlaylist(Long playlistId, Long songId, Long userId) throws DataAccessException, AccessDeniedException;

    void likePlaylist(Long playlistId, Long userId) throws DataAccessException;

    void unlikePlaylist(Long playlistId, Long userId) throws DataAccessException;

}