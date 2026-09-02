package com.music.app.service.interfaces;

import com.music.app.entity.Artist;

public interface ArtistsService {

    Long getArtistIdByUserId(Long userId);

    Long createArtist(Artist artist);

    Artist getArtistByName(String artistName);
}
