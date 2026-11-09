
package com.leave.admin.dto;

public class LoginResponse {

    private String token;
    private String tokenType;
    private String username;

    public LoginResponse(String token, String username) {
        this.token = token;
        this.tokenType = "Bearer";
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getUsername() {
        return username;
    }
}
