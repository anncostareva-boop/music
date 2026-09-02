package com.music.app.controller;


import com.music.app.entity.*;
import com.music.app.exception.DataAccessException;
import com.music.app.forms.AlbumForm;
import com.music.app.forms.SongForm;
import com.music.app.forms.SongSearchForm;
import com.music.app.service.interfaces.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import com.music.app.forms.PlaylistForm;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")

@PreAuthorize("hasRole('CLIENT')")

public class ClientController {

    private final PlaylistService playlistService;
    private final UserService userService;
    private final SongService songService;
    private final AlbumService albumService;
    private final StorageService storageService;
    private final GenreService genreService;
    private final ArtistsService artistsService;

    public ClientController(UserService userService, PlaylistService playlistService, SongService songService, AlbumService albumService, StorageService storageService, GenreService genreService, ArtistsService artistsService) {
        this.userService = userService;
        this.playlistService = playlistService;
        this.songService = songService;
        this.albumService = albumService;
        this.storageService = storageService;
        this.genreService = genreService;
        this.artistsService = artistsService;
    }

    private void populateLikedSongsModel(Authentication auth, Model model) throws SQLException {
        if (auth != null && auth.isAuthenticated()) {
            User user = userService.getUserByName(auth.getName());
            Set<Long> likedSongIds = songService.getLikedSongs(user.getUserId())
                    .stream()
                    .map(Song::getSongID)
                    .collect(Collectors.toSet());
            model.addAttribute("likedSongIds", likedSongIds);
        }
    }

    @GetMapping("/playlists")
    public String showPlaylists(Model model) {

        model.addAttribute(
                "playlists",
                playlistService.getPlaylists()
        );

        return "playlists";
    }

    @GetMapping("/create-playlist")
    public String createPlaylist(Model model) {

        model.addAttribute("form", new PlaylistForm());

        return "create-playlist";
    }

    @PostMapping("/create-playlist")
    public String createPlaylist(Authentication auth,
                                 @Valid @ModelAttribute("form") PlaylistForm playlistForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) throws IOException, SQLException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("PlaylistBio", "You need to fill out the form");
            return "create-playlist";
        }

        User user = userService.getUserByName(auth.getName());

        Playlist playlist = new Playlist();
        playlist.setPlaylistName(playlistForm.getPlaylistName());
        playlist.setPlaylistAuthor(user.getUserName());

        playlist.setUserId(user.getUserId());
        playlist.setUploadedByUserId(user.getUserId());

        MultipartFile artwork = playlistForm.getArtwork();
        if (artwork != null && !artwork.isEmpty()) {
            storageService.store(artwork);
            playlist.setArtworkFileName(artwork.getOriginalFilename());
        } else {
            playlist.setArtworkFileName("default-cover.jpg");
        }

        playlistService.addPlaylist(playlist);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Playlist has been successfully created"
        );

        return "redirect:/client/playlists";
    }

    @GetMapping("/songs")
    public String getSongs(Authentication auth, Model model) throws SQLException {
        List<Song> songs = songService.getAllSongs();
        model.addAttribute("songs", songs);

        if (auth != null && auth.isAuthenticated()) {
            User user = userService.getUserByName(auth.getName());

            List<Playlist> userPlaylists = playlistService.getPlaylistsByUser(user.getUserId());
            model.addAttribute("userPlaylists", userPlaylists);
            Set<Album> userAlbums = albumService.getAllAlbumsByArtistId(user.getUserId());
            model.addAttribute("albums", userAlbums);

            Set<Long> likedSongIds = songService.getLikedSongs(user.getUserId())
                    .stream()
                    .map(Song::getSongID)
                    .collect(Collectors.toSet());

            model.addAttribute("likedSongIds", likedSongIds);
        }

        return "songs";
    }

    @GetMapping("/albums")
    public String getAlbums(Model model) throws SQLException {
        model.addAttribute("albums", albumService.getAllAlbums());
        return "albums";
    }

    @GetMapping("/liked-songs")
    public String showLikedSongs(Authentication auth, Model model) throws SQLException {

        User user = userService.getUserByName(auth.getName());

        model.addAttribute("liked_songs", songService.getLikedSongs(user.getUserId()));

        return "liked-songs";
    }

    @GetMapping("/listening-history")
    public String listeningHistory(Authentication auth, Model model) throws SQLException {

        if (auth != null && auth.isAuthenticated()) {
            User user = userService.getUserByName(auth.getName());
            List<Song> listeningHistory = songService.getListeningHistory(user.getUserId());
            model.addAttribute("listeningHistory", listeningHistory);
            populateLikedSongsModel(auth, model);
        }
        return "listening-history";
    }

    @GetMapping("/liked-playlists")
    public String showLikedPlaylists(Authentication auth, Model model) throws SQLException {

        User user = userService.getUserByName(auth.getName());

        model.addAttribute("liked_playlists", playlistService.getLikedPlaylists(user.getUserId()));
        return "liked-playlists";
    }

    @GetMapping("/add-song")
    public String addSong(Model model) {

        model.addAttribute("form", new SongForm());

        return "add-song";
    }

    @PostMapping("/add-song")
    public String addSong(Authentication auth,
                          @Valid @ModelAttribute("form") SongForm songForm,
                          BindingResult bindingResult,
                          @RequestParam("file") MultipartFile file,
                          Model model,
                          RedirectAttributes redirectAttributes) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("SongBio", "You need to fill out the form");
            return "add-song";
        }

        Song song = new Song();
        song.setSongName(songForm.getSongName());

        Genre genre = new Genre();
        genre.setName(songForm.getGenre());

        Long genreId = genreService.getOrCreateGenreId(genre);
        song.setGenreId(genreId);

        String username = auth.getName();
        Long userId = userService.getUserIdByUsername(username);

        Long artistId = artistsService.getArtistIdByUserId(userId);

        if (artistId == null) {

            Artist artist = new Artist();
            artist.setArtistName(username);
            artist.setUserId(userId);

            artistId = artistsService.createArtist(artist);
        }

        song.setArtistId(artistId);
        song.setUserID(userId);

        song.setSongLikesAmount(0);

        if (file != null && !file.isEmpty()) {
            storageService.store(file);
            song.setFileName(file.getOriginalFilename());
        }

        MultipartFile artwork = songForm.getArtwork();

        if (artwork != null && !artwork.isEmpty()) {
            storageService.store(artwork);
            song.setArtworkFileName(artwork.getOriginalFilename());
        } else {
            song.setArtworkFileName("default-cover.jpg");
        }

        songService.addSong(song);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Song has been successfully created"
        );
        return "redirect:/profile";
    }

    @PostMapping("/delete-song/{songId}")
    public String deleteSong(Authentication auth, @PathVariable Long songId, RedirectAttributes redirectAttributes) throws IOException, SQLException {

        Song song = songService.getSongById(songId);

        songService.deleteSong(song, song.getUserID());

        storageService.delete(song.getFileName());

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Song has been successfully deleted"
        );

        return "redirect:/client/songs";
    }

    @PostMapping("/delete-playlist/{playlistId}")
    public String deletePlaylist(@PathVariable Long playlistId,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            User currentUser = userService.getUserByName(auth.getName());
            Playlist existingPlaylist = playlistService.getPlaylistbyId(playlistId);

            if (existingPlaylist == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Playlist not found.");
                return "redirect:/client/playlists";
            }

            if (!existingPlaylist.getUserId().equals(currentUser.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You can only delete your own playlists.");
                return "redirect:/client/playlists";
            }

            Playlist playlist = new Playlist();
            playlist.setPlaylistId(playlistId);
            playlistService.deletePlaylist(playlist);

            redirectAttributes.addFlashAttribute("successMessage", "Playlist has been successfully deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete playlist.");
        }

        return "redirect:/client/playlists";
    }

    @GetMapping("/recommended-songs/{userId}")
    public String recommendedSongs(@PathVariable Long userId, Model model) throws SQLException {

        model.addAttribute("recommended", songService.findRandomSongsByGenre(userId));

        return "recommended-songs";
    }

    @GetMapping("/created-songs")
    public String createdSongs(Authentication auth, Model model) throws SQLException {

        if (auth != null && auth.isAuthenticated()) {
            User user = userService.getUserByName(auth.getName());
            List<Song> createdSongs = songService.getSongsByUser(user.getUserId());
            model.addAttribute("createdSongs", createdSongs);
            populateLikedSongsModel(auth, model);
        }

        return "created-songs";
    }

    @GetMapping("/created-playlists")
    public String createdPlaylists(Authentication auth, Model model) throws SQLException {

        User user = userService.getUserByName(auth.getName());

        model.addAttribute("createdPlaylists", playlistService.getPlaylistsByUser(user.getUserId()));

        return "created-playlists";
    }

    @GetMapping("/trending")
    public String trendingSongs(Authentication auth,Model model) throws SQLException {
        List<Song> trending = songService.trendingSongs();
        model.addAttribute("trending", trending);
        populateLikedSongsModel(auth, model);
        return "trending";
    }

    @PostMapping("/toggle-like/{songId}")
    public String toggleLike(@PathVariable Long songId,
                             Authentication auth,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) throws SQLException {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.getUserByName(auth.getName());

        Set<Song> likedSongs = songService.getLikedSongs(user.getUserId());
        Set<Long> likedSongIds = likedSongs.stream()
                .map(Song::getSongID)
                .collect(Collectors.toSet());

        if (likedSongIds.contains(songId)) {
            songService.unlikeSong(songId, user.getUserId());
            redirectAttributes.addFlashAttribute("message", "Song removed from liked songs");
        } else {
            songService.likeSong(songId, user.getUserId());
            redirectAttributes.addFlashAttribute("message", "Song added to liked songs");
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/client/songs");
    }

    @PostMapping("/toggle-like-playlist/{playlistId}")
    public String toggleLikePlaylist(@PathVariable Long playlistId,
                             Authentication auth,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) throws SQLException {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.getUserByName(auth.getName());

        List<Playlist> likedPlaylists = playlistService.getLikedPlaylists(user.getUserId());
        Set<Long> likedPlaylistsIds = likedPlaylists.stream()
                .map(Playlist::getPlaylistId)
                .collect(Collectors.toSet());

        if (likedPlaylistsIds.contains(playlistId)) {
            playlistService.unlikePlaylist(playlistId, user.getUserId());
            redirectAttributes.addFlashAttribute("message", "Playlist removed from liked playlists");
        } else {
            playlistService.likePlaylist(playlistId, user.getUserId());
            redirectAttributes.addFlashAttribute("message", "Playlist added to liked playlists");
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/client/playlists");
    }


    @GetMapping("/edit-song/{songId}")
    public String editSong(@PathVariable Long songId, Authentication auth, RedirectAttributes redirectAttributes, Model model) throws SQLException, IOException {

        Song song = songService.getSongById(songId);

        if(song == null) {
            model.addAttribute("message", "Song with id " + songId + " does not exist");
            return "redirect:/client/songs";
        }

        SongForm form = new SongForm();
        form.setSongId(song.getSongID());
        form.setSongName(song.getSongName());
        form.setGenre(song.getGenreName());

        model.addAttribute("form", form);

        return "/edit-song";
    }

    @PostMapping("/edit-song/{songId}")
    public String editSong(@PathVariable Long songId, Authentication auth,  @Valid @ModelAttribute("form") SongForm songForm,
                           BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) throws SQLException, IOException {
        if(bindingResult.hasErrors()) {
            model.addAttribute("message", "Cannot edit song");
            return "edit-song";
        }

        System.out.println("Path songId = " + songId);
        System.out.println("Form songId = " + songForm.getSongId());

        Song song = songService.getSongById(songId);

        if(song == null) {
            model.addAttribute("message", "Song with id " + songId + " does not exist");
            return "redirect:/client/songs";
        }

        song.setSongName(songForm.getSongName());
        String name = songForm.getGenre();
        Genre genre = new Genre();
        genre.setName(name);
        Long genreId = genreService.getOrCreateGenreId(genre);
        song.setGenreId(genreId);

        String username = auth.getName();
        Long currentUserId = userService.getUserIdByUsername(username);

        System.out.println("New title: " + song.getSongName());
        System.out.println("New genre: " + song.getGenreName());

        songService.editSong(song, currentUserId);

        redirectAttributes.addFlashAttribute("message", "Song has been updated");

        return "redirect:/client/songs";
    }

    @GetMapping("/find-song")
    public String findSong(@RequestParam(value = "title", required = false) String title,
                           Authentication auth,
                           Model model) throws SQLException {

        List<Song> songs;

        if (title != null && !title.trim().isEmpty()) {
            songs = songService.getSongByTitle(title.trim());

            if (songs.isEmpty()) {
                model.addAttribute("message", "No song found matching: \"" + title + "\"");
            }
        } else {

            songs = songService.getAllSongs();
        }

        model.addAttribute("songs", songs);

        if (auth != null && auth.isAuthenticated()) {
            User user = userService.getUserByName(auth.getName());
            Set<Long> likedSongIds = songService.getLikedSongs(user.getUserId())
                    .stream()
                    .map(Song::getSongID)
                    .collect(Collectors.toSet());
            model.addAttribute("likedSongIds", likedSongIds);
        }

        return "songs";
    }

    @GetMapping("/playlist-songs/{playlistId}")
    public String playlistSongs(@PathVariable Long playlistId, Authentication auth, Model model) throws SQLException {
        List<Song> songs = playlistService.showSongs(playlistId);
        model.addAttribute("songsPlaylist", songs);

        Playlist playlist = playlistService.getPlaylistbyId(playlistId);
        if (playlist != null) {
            model.addAttribute("playlist", playlist);
            model.addAttribute("playlistName", playlist.getPlaylistName());
        }

        if (auth != null && auth.isAuthenticated()) {
            User user = userService.getUserByName(auth.getName());
            model.addAttribute("currentUserId", user.getUserId());

            Set<Long> likedSongIds = songService.getLikedSongs(user.getUserId())
                    .stream()
                    .map(Song::getSongID)
                    .collect(Collectors.toSet());

            model.addAttribute("likedSongIds", likedSongIds);
        }

        return "playlist-songs";
    }

    @PostMapping("/add-song-playlist")
    public String addSongToPlaylist(@RequestParam Long playlistId,
                                    @RequestParam Long songId,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) throws SQLException, AccessDeniedException {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User currentUser = userService.getUserByName(auth.getName());

        try {
            playlistService.addSongToPlaylist(playlistId, songId, currentUser.getUserId());
            redirectAttributes.addFlashAttribute("message", "Track successfully added to playlist!");
        } catch (DataAccessException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("message", "You can only edit your own playlists.");
        }

        return "redirect:/client/songs";
    }

    @PostMapping("/delete-song-playlist/{playlistId}")
    public String deleteSongFromPlaylist(@PathVariable Long playlistId, @RequestParam Long songId,
                                         Authentication auth,
                                         RedirectAttributes redirectAttributes) throws SQLException {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User currentUser = userService.getUserByName(auth.getName());

        try{
            playlistService.deleteSongFromPlaylist(playlistId, songId, currentUser.getUserId());
            redirectAttributes.addFlashAttribute("message", "Track successfully deleted from playlist!");
        }catch (DataAccessException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/client/playlist-songs/" + playlistId;
    }

    @GetMapping("/add-album")
    public String showAddAlbumForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new AlbumForm());
        }
        return "add-album";
    }

    @PostMapping("/add-album")
    public String addAlbum(Authentication auth,
                           @Valid @ModelAttribute("form") AlbumForm form,
                           BindingResult bindingResult,
                           @RequestParam(value = "artwork", required = false) MultipartFile artwork,
                           Model model,
                           RedirectAttributes redirectAttributes) throws SQLException, IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "Fill out the form correctly");
            return "add-album";
        }

        User currentUser = null;
        if (auth != null && auth.isAuthenticated()) {
            currentUser = userService.getUserByName(auth.getName());
        }

        Album album = new Album();
        album.setAlbumName(form.getAlbumName().trim());

        if (form.getArtistName() != null && !form.getArtistName().isBlank()) {
            String name = form.getArtistName().trim();
            Artist artist = artistsService.getArtistByName(name);

            if (artist != null) {
                // Case 1: Artist found by name
                album.setArtistName(artist.getArtistName());
                album.setArtistId(artist.getArtistId());
            } else {
                // Case 2: Check if this user already has an associated artist ID
                Long existingArtistId = null;
                if (currentUser != null) {
                    existingArtistId = artistsService.getArtistIdByUserId(currentUser.getUserId());
                }

                if (existingArtistId != null) {
                    // User already has an artist profile, link to that ID
                    album.setArtistId(existingArtistId);
                    album.setArtistName(name);
                } else {
                    // User does not have an artist profile yet, create a new one
                    Artist newArtist = new Artist();
                    newArtist.setArtistName(name);
                    if (currentUser != null) {
                        newArtist.setUserId(currentUser.getUserId());
                    }

                    artistsService.createArtist(newArtist);
                    Artist created = artistsService.getArtistByName(name);
                    if (created != null) {
                        album.setArtistName(created.getArtistName());
                        album.setArtistId(created.getArtistId());
                    } else {
                        album.setArtistName(name);
                    }
                }
            }
        }

        if (currentUser != null) {
            album.setUserId(currentUser.getUserId());
        }

        if (artwork != null && !artwork.isEmpty()) {
            storageService.store(artwork);
            album.setArtworkFileName(artwork.getOriginalFilename());
        } else if (form.getArtwork() != null && !form.getArtwork().isEmpty()) {
            storageService.store(form.getArtwork());
            album.setArtworkFileName(form.getArtwork().getOriginalFilename());
        } else {
            album.setArtworkFileName("default-cover.jpg");
        }

        albumService.addAlbum(album);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Album has been successfully added"
        );
        return "redirect:/client/albums";
    }

    @PostMapping("/delete-song-album/{albumId}")
    public String deleteSongFromAlbum(@PathVariable long albumId, @RequestParam Long songId, Authentication auth, RedirectAttributes redirectAttributes) throws SQLException, AccessDeniedException {
        Song song = songService.getSongById(songId);
        if(song==null){
            throw new AccessDeniedException("There is no such song");
        }

        String username = auth.getName();

        User user = userService.getUserByName(username);

        albumService.deleteSongFromAlbum(albumId, songId, user.getUserId());

        redirectAttributes.addFlashAttribute("successMessage", "Album has been successfully deleted from the album");

        return  "redirect:/client/albums";
    }

    @PostMapping("/add-song-album/{albumId}")
    public String addSongToAlbum(@PathVariable long albumId, @RequestParam Long songId, Authentication auth, RedirectAttributes redirectAttributes) throws AccessDeniedException, SQLException {
        Song song = songService.getSongById(songId);
        if(song==null){
            throw new AccessDeniedException("There is no such song");
        }

        String username = auth.getName();

        User user = userService.getUserByName(username);

        albumService.addSongToAlbum(albumId, songId, user.getUserId());

        redirectAttributes.addFlashAttribute("successMessage", "Song has been successfully added to  the album");

        return  "redirect:/client/albums";

    }

    @PostMapping("/delete-album/{albumId}")
    public String deleteAlbum(@PathVariable long albumId, Authentication auth, Model model, RedirectAttributes redirectAttributes) throws SQLException, AccessDeniedException {

        Album album = albumService.getAlbumById(albumId);
        if(album==null){
            throw new AccessDeniedException("Album has already been deleted");
        }

        String username = auth.getName();
        User user = userService.getUserByName(username);

        albumService.deleteAlbum(album, user.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Album has been successfully deleted");
        return "redirect:/client/albums";
    }

    @GetMapping("/album-songs/{albumId}")
    public String albumDetails(@PathVariable long albumId, Model model, RedirectAttributes redirectAttributes) throws SQLException {

        Album album = albumService.getAlbumById(albumId);
        model.addAttribute("album", album);
        model.addAttribute("songs", albumService.getAllSongs(albumId));

        return "album-songs";
    }

    @GetMapping("/created-albums")
    public String createdAlbums(Authentication auth, Model model) throws SQLException {
        String username = auth.getName();
        User user = userService.getUserByName(username);

        Set<Album> albums = albumService.getAllAlbumsByArtistId(user.getUserId());
        model.addAttribute("albums", albums);

        return "created-albums";
    }
}
