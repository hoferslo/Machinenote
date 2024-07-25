package com.example.machinenote;

import com.example.machinenote.models.Linija;
import com.example.machinenote.models.Role;
import com.example.machinenote.models.Zastoj;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("users.php/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @Headers("Content-Type: application/json")
    @GET("zastoji.php")  // Update with the correct endpoint
    Call<List<Zastoj>> getZastoji();

    @Headers("Content-Type: application/json")
    @POST("zastoji.php")
    Call<Void> createZastoj(@Body Zastoj zastoj);

    @Headers("Content-Type: application/json")
    @GET("linije.php")
    Call<List<Linija>> getLinije();

    @Headers("Content-Type: application/json")
    @GET("linije.php/{id}")
    Call<Linija> getLinijaById(@Path("id") int id);
}

class LoginRequest {
    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters (optional)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class LoginResponse {
    private String api_key;
    private Role role;

    public String getApiKey() {
        return api_key;
    }

    public void setApiKey(String api_key) {
        this.api_key = api_key;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
