package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Component
public interface UserStorage {
    List<User> findAll();
    User getUser(int id);
    User create(User user);
    User update(User user);
}
