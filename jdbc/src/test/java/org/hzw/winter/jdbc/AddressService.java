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
public class AddressService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Address> testWithOutTransaction(Integer userId, String address, boolean error) throws NoSuchMethodException {
        jdbcTemplate.update(INSERT_ADDRESS, userId, address);
        if (error) {
            int i = 0 / 0;
        }

        return jdbcTemplate.queryForList(ALL_ADDRESS, Address.class);
    }

    @Transactional
    public List<Address> testTransaction(Integer userId, String address, boolean error) throws NoSuchMethodException {
        jdbcTemplate.update(INSERT_ADDRESS, userId, address);

        List<Address> addresses = jdbcTemplate.queryForList(ALL_ADDRESS, Address.class);
        if (error) {
            int i = 0 / 0;
        }

        return addresses;
    }
}
