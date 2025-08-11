package com.reisfal.falbackend.model.dto;

public class LoginRequest {
<<<<<<< HEAD
    private String username;
    private String email;
    private String password;

    // Getter ve Setter'lar
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
=======
    private String identifier;
    private String password;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
>>>>>>> recover-2157
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
