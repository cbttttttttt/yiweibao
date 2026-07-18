package com.yiweibao.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String realName;

    @Column(nullable = false)
    private Integer role; // 0-操作工 1-维修工 2-管理员

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String username, String password, String realName, Integer role, String phone) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
