package com.music.app.entity;

public class Album {
    private Long albumId;
    private String albumName;
    private Long artistId;
    private String artworkFileName;
    private String artistName;
    private Long userId;

    public Album() {
    }

    public Album(Long albumId, String albumName, Long artistId, String artworkFileName) {
        this.albumId = albumId;
        this.albumName = albumName;
        this.artistId = artistId;
        this.artworkFileName = artworkFileName;
    }

    public Album(Long albumId, String albumName, Long artistId, String artworkFileName, String artistName, Long userId) {
        this.albumId = albumId;
        this.albumName = albumName;
        this.artistId = artistId;
        this.artworkFileName = artworkFileName;
        this.artistName = artistName;
        this.userId = userId;
    }

    public Long getAlbumId() { return albumId; }
    public void setAlbumId(Long albumId) { this.albumId = albumId; }

    public String getAlbumName() { return albumName; }
    public void setAlbumName(String albumName) { this.albumName = albumName; }

    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }

    public String getArtworkFileName() { return artworkFileName; }
    public void setArtworkFileName(String artworkFileName) { this.artworkFileName = artworkFileName; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}