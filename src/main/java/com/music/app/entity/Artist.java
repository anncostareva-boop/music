package com.music.app.entity;

public class Artist {

    private Long artistId;
    private String artistName;
    private Long userId;

    public Artist() {}

    public Artist(Long artistId, String artistName,  Long userId) {
        this.artistId = artistId;
        this.artistName = artistName;
        this.userId = userId;
    }

    public Long getArtistId() {
        return artistId;
    }
    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }
    public String getArtistName() {
        return artistName;
    }
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
