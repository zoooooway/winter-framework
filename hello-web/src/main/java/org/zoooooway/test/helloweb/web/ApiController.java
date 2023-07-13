package org.zoooooway.test.helloweb.web;

import org.hzw.winter.context.annotation.Autowired;
import org.hzw.winter.jdbc.DataAccessException;
import org.hzw.winter.web.mvc.annotation.GetMapping;
import org.hzw.winter.web.mvc.annotation.PathVariable;
import org.hzw.winter.web.mvc.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoooooway.test.helloweb.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
public class ApiController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @GetMapping("/api/user/{email}")
    Map<String, Boolean> userExist(@PathVariable("email") String email) {
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        try {
            userService.getUser(email);
            return Map.of("result", Boolean.TRUE);
        } catch (DataAccessException e) {
            return Map.of("result", Boolean.FALSE);
        }
    }

    @GetMapping("/api/users")
    List<User> users() {
        return userService.getUsers();
    }
}
