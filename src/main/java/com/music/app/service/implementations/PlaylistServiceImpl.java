package com.music.app.service.implementations;

import com.music.app.dao.interfaces.IPlaylistDao;
import com.music.app.entity.Playlist;
import com.music.app.entity.Song;
import com.music.app.exception.DataAccessException;
import com.music.app.service.interfaces.PlaylistService;
import com.music.app.service.interfaces.SongService;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.List;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private final IPlaylistDao playlistDAO;
    private final SongService songService;

    public PlaylistServiceImpl(IPlaylistDao playlistDAO, SongService songService) {
        this.playlistDAO = playlistDAO;
        this.songService = songService;
    }

    @Override
    public List<Playlist> getPlaylists() {
        return playlistDAO.getPlaylists();
    }

    @Override
    public void addPlaylist(Playlist playlist) {
        playlistDAO.addPlaylist(playlist);
    }

    @Override
    public void deletePlaylist(Playlist playlist) {
        playlistDAO.deletePlaylist(playlist);
    }

    @Override
    public List<Playlist> getLikedPlaylists(Long userId) {
        return playlistDAO.getLikedPlaylists(userId);
    }

    @Override
    public List<Playlist> getPlaylistsByUser(Long userId) {
        return playlistDAO.getPlaylistsByUser(userId);
    }

    @Override
    public  List<Song> showSongs(Long playlistId) {
        return playlistDAO.showSongs(playlistId);
    }

    @Override
    public Playlist getPlaylistbyId(Long playlistId) {
        return playlistDAO.getPlaylistbyId(playlistId);
    }

    @Override
    public void addSongToPlaylist(Long playlistId, Long songId, Long userId) throws DataAccessException, SQLException, AccessDeniedException {
        Song existingSong = playlistDAO.getSongbyId(songId, playlistId);
        Playlist existingPlaylist = playlistDAO.getPlaylistbyId(playlistId);

        if (existingPlaylist == null) {
            throw new DataAccessException("Playlist not found.");
        }

        if (existingSong != null) {
            throw new DataAccessException("Song is already in this playlist.");
        }

        if(!existingPlaylist.getUserId().equals(userId)){
            throw new AccessDeniedException("You alter only your own playlists.");
        }

        playlistDAO.addSongToPlaylist(playlistId, songId);
    }

    @Override
    public void deleteSongFromPlaylist(Long playlistId, Long songId, Long userId) throws DataAccessException, AccessDeniedException {
        Song existingSong = playlistDAO.getSongbyId(songId, playlistId);
        Playlist existingPlaylist = playlistDAO.getPlaylistbyId(playlistId);
        if (existingPlaylist == null) {
            throw new DataAccessException("Playlist not found.");
        }

        if (existingSong == null) {
            throw new DataAccessException("Song is already deleted.");
        }
        if(!existingPlaylist.getUserId().equals(userId)){
            throw new AccessDeniedException("You alter only your own playlists.");
        }
        playlistDAO.deleteSongFromPlaylist(playlistId, songId);
    }

    @Override
    public  void likePlaylist(Long playlistId, Long userId) {
        Playlist existingPlaylist = playlistDAO.getPlaylistbyId(playlistId);
        if(existingPlaylist == null){
            throw new DataAccessException("Playlist not found.");
        }
        playlistDAO.likePlaylist(playlistId, userId);
    }

    @Override
    public void unlikePlaylist(Long playlistId, Long userId) {
        Playlist existingPlaylist = playlistDAO.getPlaylistbyId(playlistId);
        if(existingPlaylist == null){
            throw new DataAccessException("Playlist not found.");
        }
        playlistDAO.unlikePlaylist(playlistId, userId);
    }

}