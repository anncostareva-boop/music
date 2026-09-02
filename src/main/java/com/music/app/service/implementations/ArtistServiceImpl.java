package com.music.app.service.implementations;

import com.music.app.dao.interfaces.IArtistDAO;
import com.music.app.entity.Artist;
import com.music.app.service.interfaces.ArtistsService;
import org.springframework.stereotype.Service;

@Service
public class ArtistServiceImpl implements ArtistsService {

    private final IArtistDAO  artistDAO;

    public ArtistServiceImpl(IArtistDAO artistDAO) {
        this.artistDAO = artistDAO;
    }

    @Override
    public Long getArtistIdByUserId(Long userId) {
        return artistDAO.getArtistIdByUserId(userId);
    }

    @Override
    public Long createArtist(Artist artist) {
       return artistDAO.createArtist(artist);
    }

    @Override
    public Artist getArtistByName(String artistName) {
        return artistDAO.getArtistByName(artistName);
    }
}
