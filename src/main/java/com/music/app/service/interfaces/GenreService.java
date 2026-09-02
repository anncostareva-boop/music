package com.music.app.service.interfaces;

import com.music.app.entity.Genre;

import java.util.List;

public interface GenreService {
    List<Genre> findTopGenresByUserId(Long userId);
    Long getOrCreateGenreId(Genre genre);

}
