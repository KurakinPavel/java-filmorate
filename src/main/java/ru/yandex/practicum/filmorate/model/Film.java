package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.validator.IsAfterDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    protected Set<Integer> likes;

    public Film(int id, String name, String description, LocalDate releaseDate, int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        likes = new HashSet<>();
    }

    public int getLikesSize() {
        return getLikes().size();
    }

}
