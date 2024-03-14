package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Component
public interface MpaStorage {

    List<Mpa> allMPA();

    Mpa getMPA(int id);

}
