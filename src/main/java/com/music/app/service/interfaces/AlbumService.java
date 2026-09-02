package com.music.app.service.interfaces;

import com.music.app.entity.Album;
import com.music.app.entity.Song;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface AlbumService {

    List<Album> getAllAlbums();

    void addAlbum(Album album);

    void deleteAlbum(Album album, Long userId) throws SQLException, AccessDeniedException;

    Album getAlbumById(long id);

    Album getAlbumByName(String name);

    void addSongToAlbum(Long id, Long songId, Long userId) throws AccessDeniedException;

    void deleteSongFromAlbum(Long id, Long songId, Long userId) throws AccessDeniedException;

    List<Song> getAllSongs(Long id);

    Set<Album> getAllAlbumsByArtistId(Long userId);
}