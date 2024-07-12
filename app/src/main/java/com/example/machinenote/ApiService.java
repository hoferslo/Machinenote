package com.example.machinenote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("users.php/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
}

class LoginRequest {
    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

class LoginResponse {
    private String api_key;

    public String getApiKey() {
        return api_key;
    }
}
