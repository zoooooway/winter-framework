package org.zoooooway.test.helloweb.service;

import org.hzw.winter.context.annotation.Autowired;
import org.hzw.winter.context.annotation.Component;
import org.hzw.winter.jdbc.JdbcTemplate;
import org.hzw.winter.jdbc.tx.Transactional;
import org.zoooooway.test.helloweb.web.User;

import java.util.List;


@Component
public class UserService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void initDb() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    email VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(50) NOT NULL,
                    password VARCHAR(50) NOT NULL
                )
                """;
        jdbcTemplate.update(sql);
    }

    public User getUser(String email) {
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE email = ?", User.class, email);
    }

    public List<User> getUsers() {
        return jdbcTemplate.queryForList("SELECT email, name FROM users", User.class);
    }

    @Transactional
    public User createUser(String email, String name, String password) {
        User user = new User();
        user.email = email.strip().toLowerCase();
        user.name = name.strip();
        user.password = password;
        jdbcTemplate.update("INSERT INTO users (email, name, password) VALUES (?, ?, ?)", user.email, user.name, user.password);
        return user;
    }

}
