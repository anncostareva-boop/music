package com.music.app.entity;

public class Playlist {

    private Long playlistId;
    private String playlistName;
    private Long userId;
    private Long uploadedByUserId;
    private String playlistAuthor;
    private int playlistLikesAmount;
    private String artworkFileName;

    public Playlist() {}


    public Playlist(Long playlistId, String playlistName, Long userId, Long uploadedByUserId, String artworkFileName) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.userId = userId;
        this.uploadedByUserId = uploadedByUserId;
        this.artworkFileName = artworkFileName;
    }


    public Playlist(Long playlistId, String playlistName, Long userId, Long uploadedByUserId, String playlistAuthor, int playlistLikesAmount, String artworkFileName) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.userId = userId;
        this.uploadedByUserId = uploadedByUserId;
        this.playlistAuthor = playlistAuthor;
        this.playlistLikesAmount = playlistLikesAmount;
        this.artworkFileName = artworkFileName;
    }

    // Getters and Setters

    public Long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(Long playlistId) {
        this.playlistId = playlistId;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUploadedByUserId() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(Long uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
    }

    public String getPlaylistAuthor() {
        return playlistAuthor;
    }

    public void setPlaylistAuthor(String playlistAuthor) {
        this.playlistAuthor = playlistAuthor;
    }

    public int getPlaylistLikesAmount() {
        return playlistLikesAmount;
    }

    public void setPlaylistLikesAmount(int playlistLikesAmount) {
        this.playlistLikesAmount = playlistLikesAmount;
    }

    public String getArtworkFileName() {
        return artworkFileName;
    }

    public void setArtworkFileName(String artworkFileName) {
        this.artworkFileName = artworkFileName;
    }
}