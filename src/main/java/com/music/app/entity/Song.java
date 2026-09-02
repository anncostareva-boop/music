package com.music.app.entity;

public class Song {

    private String songName;
    private Long songID;
    private Long genreId;
    private Long artistId;
    private Long userID;
    private Long albumId;
    private String fileName;
    private String artworkFileName;
    private String genreName;
    private String artistName;
    private int likes;

    public Song() {
    }

    public Song(Long songID, String songName, Long artistId, Long userID, Long albumId, Long genreId, String fileName, String artworkFileName, String genreName, String artistName, int likes) {
        this.songID = songID;
        this.songName = songName;
        this.artistId = artistId;
        this.userID = userID;
        this.albumId = albumId;
        this.genreId = genreId;
        this.fileName = fileName;
        this.artworkFileName = artworkFileName;
        this.genreName = genreName;
        this.artistName = artistName;
        this.likes = likes;
    }

    public Song(String songName, Long songID, Long genreId, Long artistId, Long userID, Long albumId, String fileName, String artworkFileName, String genreName, String artistName, int likes) {
        this.songName = songName;
        this.songID = songID;
        this.genreId = genreId;
        this.artistId = artistId;
        this.userID = userID;
        this.albumId = albumId;
        this.fileName = fileName;
        this.artworkFileName = artworkFileName;
        this.genreName = genreName;
        this.artistName = artistName;
        this.likes = likes;
    }


    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public Long getSongID() {
        return songID;
    }

    public void setSongID(Long songID) {
        this.songID = songID;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getArtworkFileName() {
        return artworkFileName;
    }

    public void setArtworkFileName(String artworkFileName) {
        this.artworkFileName = artworkFileName;
    }

    public int getSongLikesAmount() {
        return likes;
    }

    public void setSongLikesAmount(int likes) {
        this.likes = likes;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + songID +
                ", name='" + songName + '\'' +
                ", artist='" + artistName + '\'' +
                '}';
    }
    }

