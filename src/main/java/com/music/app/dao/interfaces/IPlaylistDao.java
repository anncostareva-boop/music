package com.music.app.dao.interfaces;

import com.music.app.entity.Genre;
import com.music.app.entity.Playlist;
import com.music.app.entity.Song;
import com.music.app.exception.DataAccessException;

import java.util.List;

public interface IPlaylistDao {

     List<Playlist> getPlaylists();
     void addPlaylist(Playlist playlist);
   void deletePlaylist(Playlist playlist);
     List<Playlist> getLikedPlaylists(Long userId);
     List<Playlist> getPlaylistsByUser(Long userId);
    int getLikeCountByPlaylistId(Long playlistId) throws DataAccessException;
    void likePlaylist(Long playlistId, Long userId) throws DataAccessException;
    void unlikePlaylist(Long playlistId, Long userId) throws DataAccessException;
    List<Song> showSongs(Long playlistId) throws DataAccessException;
    Playlist getPlaylistbyId(Long playlistId) throws DataAccessException;
    void addSongToPlaylist(Long playlistId, Long songId) throws DataAccessException;
    Song getSongbyId(Long songId, Long playlistId) throws DataAccessException;
    void deleteSongFromPlaylist(Long playlistId, Long songId) throws DataAccessException;
}
