package com.music.app.forms;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public class PlaylistForm {

    @NotBlank(message = "Name your playlist")
    private String playlistName;

    private String playlistAuthor;

    private MultipartFile artwork;


public  String getPlaylistName() {
        return playlistName;
    }
    public void setPlaylistName(String playlistName) {
    this.playlistName = playlistName;
    }
    public String getPlaylistAuthor() {
    return playlistAuthor;
    }
    public void setPlaylistAuthor(String playlistAuthor) {
    this.playlistAuthor = playlistAuthor;
    }
    public MultipartFile getArtwork() {
    return artwork;
    }
    public void setArtwork(MultipartFile artwork) {
    this.artwork = artwork;
    }


}
