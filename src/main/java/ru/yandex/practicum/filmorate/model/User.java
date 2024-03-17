package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class User {

    protected int id;
    @Email
    protected String email;
    @Pattern(regexp = "^\\S*$")
    protected String login;
    protected String name;
    @Past
    protected LocalDate birthday;

    public User(int id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = selectName(name, login);
        this.birthday = birthday;
    }

    private String selectName(String name, String login) {
        if (name == null || name.isEmpty()) return login;
        return name;
    }

    public Map<String, Object> userToMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("EMAIL", email);
        values.put("LOGIN", login);
        values.put("NAME", name);
        values.put("BIRTHDAY", birthday);
        return values;
    }
}
