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
    @NotBlank
    @Size(min = 1, max = 200)
    protected String description;
    @IsAfterDate(current = "1895-12-27")
    protected LocalDate releaseDate;
    @Positive
    protected int duration;
    protected Mpa mpa;
    protected List<Genre> genres;
    protected List<Director> directors;//Shtefan новое поле "Режиссёры"

    public Film(int id, String name, String description,
                LocalDate releaseDate, int duration, Mpa mpa,
                List<Genre> genres, List<Director> directors) {//Shtefan новое поле "Режиссёры"
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        this.genres = genres;
        this.directors = directors;
    }

    public List<Integer> genresToInt() {
        Set<Integer> genresWithoutDuplicates = new HashSet<>();
        for (Genre genre : genres) {
            genresWithoutDuplicates.add(genre.getId());
        }
        return new ArrayList<>(genresWithoutDuplicates);
    }

    public List<Integer> directorsToInt() {//Shtefan новое поле "Режиссёры"
        Set<Integer> directorsWithoutDuplicates = new HashSet<>();
        for (Director director : directors) {
            directorsWithoutDuplicates.add(director.getId());
        }
        return new ArrayList<>(directorsWithoutDuplicates);
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
