package org.hzw.winter.jdbc;

import org.hzw.winter.context.annotation.Autowired;
import org.hzw.winter.context.annotation.Component;
import org.hzw.winter.jdbc.tx.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.hzw.winter.jdbc.JdbcTest.ALL_USER;
import static org.hzw.winter.jdbc.JdbcTest.INSERT_USER;

/**
 * @author hzw
 */
@Component
public class UserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AddressService addressService;

    @Transactional
    public List<User> testTransaction() throws NoSuchMethodException {
        String name = "ash";
        Integer age = 18;
        jdbcTemplate.update(INSERT_USER, name, age);

        List<User> users = jdbcTemplate.queryForList(ALL_USER, User.class);
        int i = 0 / 0;
        return users;
    }

    /**
     * 当前方法声明事务，方法内调用的其他方法不声明事务
     */
    @Transactional
    public List<Object> testTransactionSubNotTX(String name, Integer age, String address) throws NoSuchMethodException {
        jdbcTemplate.update(INSERT_USER, name, age);

        List<User> users = jdbcTemplate.queryForList(ALL_USER, User.class);
        List<Address> addresses = addressService.testWithOutTransaction(users.get(0).getId(), address, true);
        List<Object> list = new ArrayList<>();
        list.add(users);
        list.add(addresses);
        return list;
    }

    /**
     * 当前方法声明事务，方法内调用的其他方法声明事务
     */
    @Transactional
    public List<Object> testTransactionSubTX(String name, Integer age, String address) throws NoSuchMethodException {
        jdbcTemplate.update(INSERT_USER, name, age);

        List<User> users = jdbcTemplate.queryForList(ALL_USER, User.class);
        List<Address> addresses = addressService.testTransaction(users.get(0).getId(), address, true);
        List<Object> list = new ArrayList<>();
        list.add(users);
        list.add(addresses);
        return list;
    }


    public List<User> testWithOutTransaction() throws NoSuchMethodException {
        String name = "cli";
        Integer age = 22;
        jdbcTemplate.update(INSERT_USER, name, age);

        return jdbcTemplate.queryForList(ALL_USER, User.class);
    }

    /**
     * 当前方法不声明事务，方法内调用的其他方法声明事务
     */
    public List<Object> testWithOutTransactionSubTX(String name, Integer age, String address) throws NoSuchMethodException {
        jdbcTemplate.update(INSERT_USER, name, age);

        List<User> users = jdbcTemplate.queryForList(ALL_USER, User.class);
        List<Address> addresses = addressService.testTransaction(users.get(0).getId(), address, true);
        List<Object> list = new ArrayList<>();
        list.add(users);
        list.add(addresses);
        return list;
    }
}
