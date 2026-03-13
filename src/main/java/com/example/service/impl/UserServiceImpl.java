package com.example.service.impl;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import com.example.util.CipherUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CipherUtil cipherUtil;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public User createUser(User user) {
        try {
            user.setPassword(cipherUtil.encrypt(user.getPassword()));
            user.setIdCard(cipherUtil.encrypt(user.getIdCard()));
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now().format(formatter));
        user.setUpdateTime(LocalDateTime.now().format(formatter));
        userMapper.insert(user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        try {
            if (user.getPassword() != null) {
                user.setPassword(cipherUtil.encrypt(user.getPassword()));
            }
            if (user.getIdCard() != null) {
                user.setIdCard(cipherUtil.encrypt(user.getIdCard()));
            }
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
        user.setUpdateTime(LocalDateTime.now().format(formatter));
        userMapper.update(user);
        return getUserById(user.getId());
    }

    @Override
    public User deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.deleteById(id);
        return user;
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            try {
                user.setPassword(cipherUtil.decrypt(user.getPassword()));
                user.setIdCard(cipherUtil.decrypt(user.getIdCard()));
            } catch (Exception e) {
                throw new RuntimeException("解密失败", e);
            }
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userMapper.selectAll();
        for (User user : users) {
            try {
                user.setPassword(cipherUtil.decrypt(user.getPassword()));
                user.setIdCard(cipherUtil.decrypt(user.getIdCard()));
            } catch (Exception e) {
                throw new RuntimeException("解密失败", e);
            }
        }
        return users;
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            try {
                user.setPassword(cipherUtil.decrypt(user.getPassword()));
                user.setIdCard(cipherUtil.decrypt(user.getIdCard()));
            } catch (Exception e) {
                throw new RuntimeException("解密失败", e);
            }
        }
        return user;
    }

    @Override
    public User deactivateUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(0);
        user.setUpdateTime(LocalDateTime.now().format(formatter));
        userMapper.update(user);
        return getUserById(id);
    }
}
