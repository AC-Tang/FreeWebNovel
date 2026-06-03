package com.example.freefiction.domain;

import lombok.Data;

@Data
public class Captcha {
    private String captchaId;
    private String captcha;
    private String email;
    private String code;
}
