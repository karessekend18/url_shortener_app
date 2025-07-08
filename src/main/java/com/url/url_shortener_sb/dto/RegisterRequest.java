package com.url.url_shortener_sb.dto;

import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private Set<String> role;
}
