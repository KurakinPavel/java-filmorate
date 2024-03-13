package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    protected int numerator;
    protected final Map<Integer, User> users;

    public InMemoryUserStorage() {
        numerator = 0;
        users = new HashMap<>();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(int id) {
        User user = users.get(id);
        if (user == null) throw new NoSuchElementException("Пользователь с id " + id + " не найден.");
        return user;
    }

    @Override
    public User create(User user) {
        user.setId(++numerator);
        users.put(numerator, user);
        log.info("Добавлен новый пользователь с id {} и логином {}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == 0) {
            user.setId(++numerator);
            users.put(numerator, user);
            log.info("Добавлен новый пользователь с id {} и логином {}", + user.getId(), user.getLogin());
        } else if (!users.containsKey(user.getId())) {
            throw new NoSuchElementException("Пользователь с id " + user.getId() + " и логином " + user.getLogin() +
                    " не найден. Обновление отклонено.");
        } else {
            users.put(user.getId(), user);
            log.info("Обновлены данные пользователя с id {} и логином {}", user.getId(), user.getLogin());
        }
        return user;
    }

    @Override
    public Map<String, String> addInFriends(int id, int friendId) {
        User user = users.get(id);
        Set<Integer> friendFriends = users.get(friendId).getFriends();
        friendFriends.add(id);
        log.info("Пользователь с id {} добавился в друзья к пользователю с id {}", id, friendId);
        return Map.of("result", "Пользователь с id " + id + " добавился в друзья к пользователю с id "
                + friendId);
    }

    @Override
    public Map<String, String> removeFromFriends(int id, int friendId) {
        User user = users.get(id);
        Set<Integer> friendFriends = users.get(friendId).getFriends();
        friendFriends.remove(id);
        log.info("Пользователь с id {} удалился из друзей у пользователя с id {}", id, friendId);
        return Map.of("result", "Пользователь с id " + id + " удалился из друзей у пользователя с id "
                + friendId);
    }

    @Override
    public List<User> getFriendsOfUser(int id) {
        Set<Integer> userFriends = users.get(id).getFriends();
        List<User> friends = new ArrayList<>();
        for (int friendId : userFriends) {
            friends.add(users.get(friendId));
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        Set<Integer> userFriends = users.get(id).getFriends();
        Set<Integer> otherUserFriends = users.get(otherId).getFriends();
        Set<Integer> intersection = new HashSet<>(userFriends);
        intersection.retainAll(otherUserFriends);
        List<User> commonFriends = new ArrayList<>();
        for (int friendId : intersection) {
            commonFriends.add(users.get(friendId));
        }
        return commonFriends;
    }

}
