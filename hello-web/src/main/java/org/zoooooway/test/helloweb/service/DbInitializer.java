package org.zoooooway.test.helloweb.service;

import jakarta.annotation.PostConstruct;
import org.hzw.winter.context.annotation.Autowired;
import org.hzw.winter.context.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DbInitializer {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @PostConstruct
    void init() {
        logger.info("init database...");
        userService.initDb();
    }
}
