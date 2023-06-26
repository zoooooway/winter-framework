package org.hzw.winter.jdbc;

import org.hzw.winter.context.annotation.Autowired;
import org.hzw.winter.context.annotation.Component;
import org.hzw.winter.jdbc.tx.Transactional;

import java.util.List;

import static org.hzw.winter.jdbc.JdbcTest.*;

/**
 * @author hzw
 */
@Component
public class UserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public List<User> testTransaction() throws NoSuchMethodException {
        String name = "ash";
        Integer age = 18;
        jdbcTemplate.update(INSERT_USER, name, age);

        List<User> users = jdbcTemplate.queryForList(ALL_USER, User.class);
        int i = 0 / 0;
        return users;
    }


    public List<User> testWithOutTransaction() throws NoSuchMethodException {
        String name = "cli";
        Integer age = 22;
        jdbcTemplate.update(INSERT_USER, name, age);

        return jdbcTemplate.queryForList(ALL_USER, User.class);
    }
}
