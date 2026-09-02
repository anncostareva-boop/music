package com.music.app.forms;

import com.music.app.entity.Genre;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public class SongForm {

    @NotBlank (message = "Your song has to be titled")
    private String songName;
    @NotBlank (message = "Type in the genre")
    private String genre;
    private MultipartFile artwork;
    private Long songId;

    public String getSongName() {
        return songName;
    }
    public void setSongName(String songName) {
        this.songName = songName;
    }
    public String getGenre() {
        return genre;
    }
    public void setGenre(String genre) {
        this.genre = genre;
    }
    public MultipartFile getArtwork() {
        return artwork;
    }
    public void setArtwork(MultipartFile artwork) {
        this.artwork = artwork;
    }
    public Long getSongId() {
        return songId;
    }
    public void setSongId(Long songId) {
        this.songId = songId;
    }
}
