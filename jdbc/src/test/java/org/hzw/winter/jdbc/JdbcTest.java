package org.hzw.winter.jdbc;

import org.hzw.winter.context.bean.AnnotationConfigApplicationContext;
import org.hzw.winter.context.property.PropertyResolver;
import org.hzw.winter.context.util.YamlUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * @author hzw
 */
public class JdbcTest {
    public static final String DROP_USER = "DROP TABLE IF EXISTS users";
    public static final String DROP_ADDRESS = "DROP TABLE IF EXISTS addresses";

    public static final String CREATE_USER = "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(255) NOT NULL, age INTEGER)";
    public static final String CREATE_ADDRESS = "CREATE TABLE addresses (id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER NOT NULL, address VARCHAR(255) NOT NULL)";

    public static final String INSERT_USER = "INSERT INTO users (name, age) VALUES (?, ?)";
    public static final String INSERT_ADDRESS = "INSERT INTO addresses (userId, address) VALUES (?, ?)";

    public static final String UPDATE_USER = "UPDATE users SET name = ?, age = ? WHERE id = ?";
    public static final String UPDATE_ADDRESS = "UPDATE addresses SET address = ? WHERE id = ?";

    public static final String DELETE_USER = "DELETE FROM users WHERE id = ?";
    public static final String DELETE_ADDRESS_BY_USERID = "DELETE FROM addresses WHERE userId = ?";

    public static final String ALL_USER = "SELECT * FROM users";
    public static final String SELECT_USER = "SELECT * FROM users WHERE id = ?";
    public static final String SELECT_USER_NAME = "SELECT name FROM users WHERE id = ?";
    public static final String SELECT_USER_AGE = "SELECT age FROM users WHERE id = ?";
    public static final String ALL_ADDRESS = "SELECT * FROM addresses";
    public static final String SELECT_ADDRESS_BY_USERID = "SELECT * FROM addresses WHERE userId = ?";

    @Test
    public void test() throws Exception {
        Map<String, Object> map = YamlUtils.loadYaml("test.yml");
        PropertyResolver propertyResolver = new PropertyResolver(map);
        var context = new AnnotationConfigApplicationContext(JdbcTest.class, propertyResolver);
        JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);

        // 初始化表
        jdbcTemplate.update(DROP_USER);
        jdbcTemplate.update(DROP_ADDRESS);
        jdbcTemplate.update(CREATE_USER);
        jdbcTemplate.update(CREATE_ADDRESS);

        Assertions.assertEquals(0, jdbcTemplate.queryForList(SELECT_USER, User.class).size());
        Assertions.assertNull(jdbcTemplate.queryForObject(SELECT_USER, User.class, 1));

        User u = new User();
        String name = "ash";
        Integer age = 18;
        int update = jdbcTemplate.update(INSERT_USER, name, age);
        Assertions.assertEquals(1, jdbcTemplate.queryForList(ALL_USER, User.class).size());

        User user = jdbcTemplate.queryForList(ALL_USER, User.class).get(0);

        Address ad = new Address();
        ad.setUserId(user.getId());
        ad.setAddress("hangzhou");
        jdbcTemplate.update(INSERT_ADDRESS, ad.getUserId(), ad.getAddress());
        Assertions.assertEquals(1, jdbcTemplate.queryForList(ALL_ADDRESS, Address.class).size());
        Assertions.assertNotNull(jdbcTemplate.queryForObject(SELECT_ADDRESS_BY_USERID, Address.class, user.getId()));

        Assertions.assertEquals(age, jdbcTemplate.queryForList(SELECT_USER_AGE, User.class, user.getId()).get(0).getAge());
        Assertions.assertEquals(age, jdbcTemplate.queryForObject(SELECT_USER_AGE, User.class, user.getId()).getAge());

        Assertions.assertEquals(user.getAge(), jdbcTemplate.queryForList(SELECT_USER_AGE, Number.class, user.getId()).get(0));
        Assertions.assertEquals(user.getAge(), jdbcTemplate.queryForObject(SELECT_USER_AGE, Number.class, user.getId()));
        Assertions.assertEquals(user.getName(), jdbcTemplate.queryForList(SELECT_USER_NAME, String.class, user.getId()).get(0));
        Assertions.assertEquals(user.getName(), jdbcTemplate.queryForObject(SELECT_USER_NAME, String.class, user.getId()));

        jdbcTemplate.update(UPDATE_USER, "cli", 22, user.getId());
        Assertions.assertEquals("cli", jdbcTemplate.queryForObject(SELECT_USER_NAME, String.class, user.getId()));
        Assertions.assertEquals(22, jdbcTemplate.queryForObject(SELECT_USER_AGE, Number.class, user.getId()));

        Assertions.assertNotNull(jdbcTemplate.queryForObject(SELECT_USER, User.class, user.getId()));
        jdbcTemplate.update(DELETE_USER, user.getId());
        Assertions.assertNull(jdbcTemplate.queryForObject(SELECT_USER, User.class, user.getId()));


    }


    @Test
    public void testTx() throws Exception {
        Map<String, Object> map = YamlUtils.loadYaml("test.yml");
        PropertyResolver propertyResolver = new PropertyResolver(map);
        var context = new AnnotationConfigApplicationContext(JdbcTest.class, propertyResolver);
        JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);

        // 初始化表
        jdbcTemplate.update(DROP_USER);
        jdbcTemplate.update(DROP_ADDRESS);
        jdbcTemplate.update(CREATE_USER);
        jdbcTemplate.update(CREATE_ADDRESS);

        UserService userService = context.getBean("userService", UserService.class);
        try {
            List<User> users = userService.testTransaction();
            System.out.println(users);
        } catch (Exception e){
            e.printStackTrace();
        }
        List<User> users = jdbcTemplate.queryForList(ALL_USER, User.class);
        System.out.println(users);
        List<User> users1 = userService.testWithOutTransaction();
        System.out.println(users1);
    }
}
