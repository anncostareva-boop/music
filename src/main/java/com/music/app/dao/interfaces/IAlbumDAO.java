package com.music.app.dao.interfaces;

import com.music.app.entity.Album;
import com.music.app.entity.Song;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface IAlbumDAO {

    List<Album> getAllAlbums();
     void addAlbum(Album album);
     void deleteAlbum(Album album) throws SQLException;
     Album getAlbumById(long id);
    Album getAlbumByName(String name);
    void addSongToAlbum(Long id, Long songId);
    void deleteSongFromAlbum(Long id, Long songId);
    List<Song> getAllSongs(Long id);
    Set<Album> getAllAlbumsByArtistId(Long userId);
}
