package com.yiweibao.dto;

public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private Integer role;
    private String realName;

    public LoginResponse(String token, Long userId, String username, Integer role, String realName) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.realName = realName;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Integer getRole() { return role; }
    public String getRealName() { return realName; }
}
