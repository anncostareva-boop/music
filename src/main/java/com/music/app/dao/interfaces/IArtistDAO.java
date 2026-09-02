package com.music.app.dao.interfaces;

import com.music.app.entity.Artist;

public interface IArtistDAO {
    Long getArtistIdByUserId(Long userId);

    Long createArtist(Artist artist);

    Artist getArtistByName(String artistName);
}
