package com.music.app.forms;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;


public class AlbumForm {
@NotBlank
    private String albumName;

    @NotBlank
    private String artistName;

    private MultipartFile artwork;

public String getAlbumName() {
    return albumName;
}
public void setAlbumName(String albumName) {
    this.albumName = albumName;
}
public String getArtistName() {
    return artistName;
}
public void setArtistName(String artistName) {
    this.artistName = artistName;
}
public MultipartFile getArtwork() {
    return artwork;
}
public void setArtwork(MultipartFile artwork) {
    this.artwork = artwork;
}
}
