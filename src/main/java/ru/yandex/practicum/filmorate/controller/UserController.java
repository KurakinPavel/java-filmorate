package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    protected int numerator;
    protected final Map<Integer, User> users;

    public UserController() {
        numerator = 0;
        users = new HashMap<>();
    }

    @GetMapping
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        user.setId(++numerator);
        users.put(numerator, user);
        log.info("Добавлен новый пользователь с id {} и логином {}", + user.getId(), user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == 0) {
            user.setId(++numerator);
            users.put(numerator, user);
            log.info("Добавлен новый пользователь с id {} и логином {}", + user.getId(), user.getLogin());
        } else if (!users.containsKey(user.getId())) {
            throw new NoSuchElementException("Пользователь с id " + user.getId() + " и логином " + user.getLogin() +
                    " не найден. Обновление отклонено.");
        } else {
            users.put(user.getId(), user);
            log.info("Обновлены данные пользователя с id {} и логином {}", + user.getId(), user.getLogin());
        }
        return user;
    }
}
