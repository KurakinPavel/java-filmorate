package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.validator.IsAfterDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Data
public class Film {

    protected int id;
    @NotBlank
    protected String name;
    @NotBlank @Size(min = 1, max = 200)
    protected String description;
    @IsAfterDate(current = "1895-12-27")
    protected LocalDate releaseDate;
    @Positive
    protected int duration;
    protected IdContainer mpa;
    protected List<IdContainer> genres;

    public Film(int id, String name, String description,
                LocalDate releaseDate, int duration, IdContainer mpa, List<IdContainer> genres) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        this.genres = genres;
    }

    public List<Integer> genresToInt() {
        List<Integer> genresInInt = new ArrayList<>();
        for (IdContainer genre : genres) {
            genresInInt.add(genre.getId());
        }
        return genresInInt;
    }

    public Map<String, Object> filmToMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("NAME", name);
        values.put("DESCRIPTION", description);
        values.put("RELEASE_DATE", releaseDate);
        values.put("DURATION", duration);
        values.put("MPA_ID", mpa.getId());
        return values;
    }
}
