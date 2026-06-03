package com.example.freefiction.domain;

import lombok.Data;

@Data
public class UsersLogin {
    private String username;
    private String password;
    private String email;
}
