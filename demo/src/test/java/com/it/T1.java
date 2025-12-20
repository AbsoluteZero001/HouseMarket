package com.it;

import com.it.dao.UserDao;
import com.it.pojo.User;
import com.it.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = DemoApplication.class)
public class T1 {
    @Autowired
    private UserService userService;

    @Test
    void test() {
        userService.show();
    }
    
    @Test
    void test2() {
        User user = new User();
        user.setUsername("qazqaz");
        user.setPassword("123456789");
        user.setRole("admin");
        user.setStatus("active");
        userService.register(user);
    }

}