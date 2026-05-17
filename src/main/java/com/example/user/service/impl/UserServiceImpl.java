package com.example.user.service.impl;

import com.example.user.entity.User;
import com.example.user.entity.dto.UserDTO;
import com.example.user.entity.vo.UserVO;
import com.example.user.mapper.UserMapper;
import com.example.user.service.UserService;
import com.example.user.util.CipherUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (userMapper.checkUsernameExists(userDTO.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户实体
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setRealName(userDTO.getRealName());
        user.setPhone(userDTO.getPhone());
        user.setEmail(userDTO.getEmail());
        user.setStatus(0); // 正常状态
        user.setRole("USER"); // 默认普通用户角色

        // 加密存储密码和身份证号
        user.setEncryptedPassword(userDTO.getPassword());
        user.setEncryptedIdCard(userDTO.getIdCard());

        // 保存用户
        userMapper.insert(user);

        return convertToVO(user);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public UserVO getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public List<UserVO> getAllUsers() {
        List<User> users = userMapper.selectAll();
        return users.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<UserVO> getAllActiveUsers() {
        List<User> users = userMapper.selectAllActive();
        return users.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户是否已注销
        if (existingUser.getStatus() == 1) {
            throw new RuntimeException("用户已注销，无法修改");
        }

        // 更新用户信息
        existingUser.setRealName(userDTO.getRealName());
        existingUser.setPhone(userDTO.getPhone());
        existingUser.setEmail(userDTO.getEmail());

        // 如果提供了新密码，则加密更新
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setEncryptedPassword(userDTO.getPassword());
        }

        // 如果提供了新身份证号，则加密更新
        if (userDTO.getIdCard() != null && !userDTO.getIdCard().isEmpty()) {
            existingUser.setEncryptedIdCard(userDTO.getIdCard());
        }

        userMapper.update(existingUser);

        return convertToVO(existingUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 物理删除，仅管理员可操作
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUser(Long id) {
        // 逻辑删除（注销），用户自己操作
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == 1) {
            throw new RuntimeException("用户已注销");
        }
        userMapper.cancelUser(id);
    }

    @Override
    public UserVO login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() == 1) {
            throw new RuntimeException("用户已注销");
        }

        // 验证密码
        String decryptedPassword = CipherUtil.decrypt(user.getPassword());
        if (!decryptedPassword.equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        return convertToVO(user);
    }

    /**
     * 将User转换为UserVO，并对敏感信息进行脱敏处理
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setRole(user.getRole());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setCancelTime(user.getCancelTime());

        try {
            String decryptedIdCard = CipherUtil.decrypt(user.getIdCard());
            vo.setMaskedIdCard(maskIdCard(decryptedIdCard));
        } catch (Exception e) {
            vo.setMaskedIdCard(maskIdCard(user.getIdCard()));
        }

        return vo;
    }

    /**
     * 身份证号脱敏处理
     * 显示前3位和后4位，中间用*代替
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }
}
