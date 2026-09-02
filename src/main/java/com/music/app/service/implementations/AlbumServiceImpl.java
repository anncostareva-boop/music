package com.music.app.service.implementations;

import com.music.app.dao.interfaces.IAlbumDAO;
import com.music.app.entity.Album;
import com.music.app.entity.Song;
import com.music.app.entity.User;
import com.music.app.service.interfaces.AlbumService;
import com.music.app.service.interfaces.UserService;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AlbumServiceImpl implements AlbumService {

    private final IAlbumDAO albumDAO;
    private final UserService userService;

    public AlbumServiceImpl(IAlbumDAO albumDAO, UserService userService) {
        this.albumDAO = albumDAO;
        this.userService = userService;
    }

    @Override
    public List<Album> getAllAlbums() {
        return albumDAO.getAllAlbums();
    }

    @Override
    public void addAlbum(Album album) {
        albumDAO.addAlbum(album);
    }

    @Override
    public void deleteAlbum(Album album, Long userId) throws SQLException, AccessDeniedException {
        User user = userService.getUserById(userId);
            if (album == null || user == null) {
                return;
            }

            Album existingAlbum = albumDAO.getAlbumById(album.getAlbumId());
            if (existingAlbum == null) {
                return;
            }

            boolean isAdmin = "ADMIN".equalsIgnoreCase(String.valueOf(user.getRole()));
            boolean isOwner = Objects.equals(existingAlbum.getUserId(), user.getUserId());

            if (!isAdmin && !isOwner) {
                throw new AccessDeniedException("You can edit only your own albums.");
            }

            albumDAO.deleteAlbum(existingAlbum);
        }

    @Override
    public Album getAlbumById(long id) {
        return albumDAO.getAlbumById(id);
    }

    @Override
    public Album getAlbumByName(String name) {
        return albumDAO.getAlbumByName(name);
    }

    @Override
    public  void addSongToAlbum(Long id, Long songId, Long userId) throws AccessDeniedException {

        Album existingAlbum =  albumDAO.getAlbumById(id);
        if (!Objects.equals(existingAlbum.getUserId(), userId)) {
            throw new AccessDeniedException("You can edit only your own albums.");
        }
        albumDAO.addSongToAlbum(id, songId);
    }

    @Override
    public void deleteSongFromAlbum(Long id, Long songId, Long userId) throws AccessDeniedException {

        Album existingAlbum = albumDAO.getAlbumById(id);
        if (existingAlbum == null) {
            throw new AccessDeniedException("Album not found.");
        }

        if (!Objects.equals(existingAlbum.getUserId(), userId)) {
            throw new AccessDeniedException("You can edit only your own albums.");
        }

        albumDAO.deleteSongFromAlbum(id, songId);

    }

    @Override
    public List<Song> getAllSongs(Long id) {
        return albumDAO.getAllSongs(id);
    }

    @Override
    public Set<Album> getAllAlbumsByArtistId(Long userId) {
        return albumDAO.getAllAlbumsByArtistId(userId);
    }
}
