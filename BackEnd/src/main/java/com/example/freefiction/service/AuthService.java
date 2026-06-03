package com.example.freefiction.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.freefiction.domain.UsersLogin;
import com.example.freefiction.entity.Users;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.UsersService;
import com.example.freefiction.service.sys.impl.BookshelvesServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsersService usersService;        // 纯业务
    private final PasswordEncoder passwordEncoder;  // 只在这里出现
    private final BookshelvesServiceImpl bookshelvesService;

    public ServiceResult<Users> login(UsersLogin userLogin){
        String username = userLogin.getUsername();
        String password = userLogin.getPassword();
        String email = userLogin.getEmail();

        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        System.out.println("查询用户信息");
        System.out.println( userLogin);
        System.out.println( userLogin.getEmail());
        System.out.println( userLogin.getUsername());
        if(!email.isEmpty()){
            queryWrapper.eq("email", email);
        }
        if(!username.isEmpty()) {
            queryWrapper.eq("username", username);
        }
        Users user = usersService.getOne(queryWrapper);
        // 简单的密码验证（实际项目中应该使用加密密码）
        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            return ServiceResult.ok(user);
        }
        return ServiceResult.fail("用户名或密码错误");
    }

    public ServiceResult<Users> register(UsersLogin userLogin){
        if (userLogin.getEmail().isEmpty() || userLogin.getPassword().isEmpty()) {
            return ServiceResult.fail("请填写邮箱和密码");
        }
        if (usersService.getOne(new QueryWrapper<Users>().eq("email", userLogin.getEmail())) != null) {
            return ServiceResult.fail("用户已存在");
        }
        Users user = new Users();
        user.setUsername("用户"+ userLogin.getEmail());
        user.setPasswordHash(passwordEncoder.encode(userLogin.getPassword()));
        user.setRoleName("ROLE_USER");
        user.setEmail(userLogin.getEmail());
        boolean isRegistered = usersService.save(user);
        bookshelvesService.createBookshelf(user.getId(),"默认书架");
        if (isRegistered) {
            return ServiceResult.ok(user);
        }
        return ServiceResult.fail("注册失败,用户已存在");
    }
}
