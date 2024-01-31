package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.IsAfter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Film {

    protected int id;
    @NotBlank
    protected String name;
    @NotBlank @Size(min = 1, max = 200)
    protected String description;
    @IsAfter(current = "1895-12-27")
    protected LocalDate releaseDate;
    @Positive
    protected int duration;

}