package com.it.service;

import com.it.dao.UserDao;
import com.it.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserserviceImpl implements UserService{
    @Autowired
    private UserDao userDao;
    
    @Override
    public void show() {
        //打印数据库信息
        System.out.println(userDao.show());
        System.out.println("show()方法被调用");
    }

    @Override
    public void register(User user) {
        int result = userDao.register(user);
        if (result <= 0) {
            throw new RuntimeException("注册失败");
        }
    }
}