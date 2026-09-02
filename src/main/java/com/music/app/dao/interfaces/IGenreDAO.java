package com.music.app.dao.interfaces;

import com.music.app.entity.Genre;

import java.util.List;
import java.util.Set;

public interface IGenreDAO {

    List<Genre> findTopGenresByUserId(Long userId);
    Genre findByName(String name);

    Long addGenreId(Genre genre);
}
