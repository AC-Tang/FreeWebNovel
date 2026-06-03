package com.example.freefiction.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.freefiction.entity.Users;
import com.example.freefiction.service.sys.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthCustomUserDetailsService implements UserDetailsService {
    private final UsersService usersService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> userOptional = Optional.ofNullable(usersService.getOne(new QueryWrapper<Users>().eq("username", username)));
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("用户不存在");
        }

        Users user = userOptional.get();

        // 创建 GrantedAuthority 对象
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(user.getRoleName());
        // 创建 UserDetails 实例

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }
}
