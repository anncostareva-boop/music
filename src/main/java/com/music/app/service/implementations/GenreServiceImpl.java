package com.music.app.service.implementations;

import com.music.app.dao.implementations.GenreDAOImpl;
import com.music.app.dao.interfaces.IGenreDAO;
import com.music.app.entity.Genre;
import com.music.app.service.interfaces.GenreService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GenreServiceImpl implements GenreService {

    private final IGenreDAO  genreDAO;

    public GenreServiceImpl(IGenreDAO genreDAO) {
        this.genreDAO = genreDAO;
    }

    @Override
    public List<Genre> findTopGenresByUserId(Long userId) {
        return genreDAO.findTopGenresByUserId(userId);
    }

    @Override
    public Long getOrCreateGenreId(Genre genre) {

        if (genre == null || genre.getName() == null || genre.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Genre name cannot be null or empty");
        }

        String cleanName = genre.getName().trim();
        genre.setName(cleanName);

        Genre exists = genreDAO.findByName(genre.getName());

        if (exists != null) {
            return exists.getId();
        }
        return genreDAO.addGenreId(genre);
    }
}
