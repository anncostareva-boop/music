package com.music.app.controller;

import com.music.app.entity.Album;
import com.music.app.entity.Artist;
import com.music.app.entity.Song;
import com.music.app.entity.User;
import com.music.app.enums.Role;
import com.music.app.forms.AlbumForm;
import com.music.app.forms.RegisterForm;
import com.music.app.service.interfaces.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")

public class AdminController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SongService songService;
    private final PlaylistService playlistService;
    private final AlbumService albumService;
    private final StorageService storageService;
    private final ArtistsService  artistService;
    public AdminController(UserService userService, PasswordEncoder passwordEncoder,  SongService songService, PlaylistService playlistService, AlbumService albumService,  StorageService storageService,  ArtistsService artistService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.songService = songService;
        this.playlistService = playlistService;
        this.albumService = albumService;
        this.storageService = storageService;
        this.artistService = artistService;
    }

    @GetMapping("/users")
    public String getUsers(Model model) throws SQLException {

        model.addAttribute("users", userService.getAllUsers()
        );

        return "admin/users";
    }

    @GetMapping("/songs")
    public String getSongs(Model model) throws SQLException {
        model.addAttribute("songs", songService.getAllSongs());
        return "admin/songs";
    }

@GetMapping("/albums")
public String getAlbums(Model model) throws SQLException {
        model.addAttribute("albums", albumService.getAllAlbums());
        return "admin/albums";
}

@GetMapping("/add-album")
public String addAlbum(Model model) throws SQLException {
    model.addAttribute("form", new AlbumForm());
        return "admin/add-album";
}

@PostMapping("/add-album")
public String addAlbum(Authentication auth,
                       @Valid @ModelAttribute("form") AlbumForm form,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) throws SQLException, IOException {

    if (bindingResult.hasErrors()) {
        model.addAttribute("message", "Fill out the form correctly");
        return "admin/add-album";
    }

    Album album = new Album();
    album.setAlbumName(form.getAlbumName());

    if(form.getArtistName() != null && !form.getArtistName().equals("")) {
        String name = form.getArtistName();
        Artist artist = artistService.getArtistByName(name);
        if (artist == null) {
           Artist newArtist = new Artist();
           newArtist.setArtistName(name);
           artistService.createArtist(newArtist);
           artist.setArtistName(newArtist.getArtistName());
        }
        if(artist != null) {
            album.setArtistName(artist.getArtistName());
            album.setArtistId(artist.getArtistId());
        }
    }

    MultipartFile artwork = form.getArtwork();
    if (artwork != null && !artwork.isEmpty()) {
        storageService.store(artwork);
        album.setArtworkFileName(artwork.getOriginalFilename());
    } else {
        album.setArtworkFileName("default-cover.jpg");
    }

    albumService.addAlbum(album);

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Album has been successfully added"
    );
    return "redirect:/admin/albums";
}

    @GetMapping("/add-user")
    public String addUser(Model model, RegisterForm form) throws SQLException {

        model.addAttribute("form", form);

        return "admin/add-user";
    }

    @PostMapping("/add-user")
    public String addUser(Authentication auth, @Valid @ModelAttribute("form") RegisterForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) throws SQLException {

        if(bindingResult.hasErrors()) {
            model.addAttribute("message", "Fill out the form correctly");
            return "admin/add-user";
        } else{
            User user = new User();
            user.setUserName(form.getUserName());
            user.setPhoneNumber(form.getPhoneNumber());
            user.setEmail(form.getEmail());
            user.setPassword(passwordEncoder.encode(form.getPassword()));
            user.setRole(Role.CLIENT);
            userService.addUser(user);
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "User has been successfully added"
        );

        return "redirect:/admin";
    }

    @GetMapping("/user-details/users/{id}")
public String getUserDetails( Model model, @PathVariable long id) throws SQLException {

        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("userSongs", songService.getSongsByUser(id));
        model.addAttribute("userPlaylists", playlistService.getPlaylistsByUser(id));

        return "admin/user-details";
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) throws SQLException {
        model.addAttribute("usersCount", userService.getAllUsers().size());
        model.addAttribute("songsCount", songService.getAllSongs().size());
        model.addAttribute("albumsCount", albumService.getAllAlbums().size());
        model.addAttribute("playlistsCount", playlistService.getPlaylists().size());
        return "admin/dashboard";
    }

    @PostMapping("/songs/{songId}/delete")
    public String deleteSong(@PathVariable long songId,  Model model, RedirectAttributes redirectAttributes) throws SQLException, AccessDeniedException {
Song song = songService.getSongById(songId);
if(song!=null){
    songService.deleteSong(song);
    redirectAttributes.addFlashAttribute("successMessage", "Song has been successfully deleted");
}
        return  "redirect:/admin/songs";
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
        return "redirect:/admin/albums";
    }


}
