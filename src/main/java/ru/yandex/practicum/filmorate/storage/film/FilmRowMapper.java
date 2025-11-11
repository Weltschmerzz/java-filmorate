package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film f = new Film();
        f.setId(rs.getLong("id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));
        Date date = rs.getDate("release_date");
        if (date != null) f.setReleaseDate(date.toLocalDate());
        f.setDuration(rs.getInt("duration"));
        Mpa mpa = new Mpa();
        mpa.setId(rs.getLong("mpa_id"));
        String mpaName;
        try {
            mpaName = rs.getString("mpa_name");
        } catch (SQLException ignore) {
            mpaName = null;
        }
        mpa.setName(mpaName);
        f.setMpa(mpa);
        return f;
    }
}