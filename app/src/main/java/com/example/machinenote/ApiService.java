package com.example.machinenote;

import com.example.machinenote.models.Imenik;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.RezervniDel;
import com.example.machinenote.models.Role;
import com.example.machinenote.models.Sifrant;
import com.example.machinenote.models.SklopLinije;
import com.example.machinenote.models.Zastoj;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @GET("connection.php")
    Call<ServerResponse> checkConnection();

    @Headers("Content-Type: application/json")
    @POST("users.php/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @Headers("Content-Type: application/json")
    @GET("rezervni_deli.php")
    Call<List<RezervniDel>> getRezervniDel();

    @Headers("Content-Type: application/json")
    @POST("rezervni_deli.php")
    Call<Void> createRezervniDeli(@Body RezervniDel rezervniDel);

    @Headers("Content-Type: application/json")
    @PUT("rezervni_deli.php/{id}")
    Call<Void> updateRezervniDel(@Path("id") int id, @Body RezervniDel rezervniDel);

    @Headers("Content-Type: application/json")
    @GET("rezervni_deli.php/{id}")
    Call<RezervniDel> getRezervniDelById(@Path("id") int id);


    @Headers("Content-Type: application/json")
    @GET("zastoji.php")
        // Update with the correct endpoint
    Call<List<Zastoj>> getZastoji();

    @Headers("Content-Type: application/json")
    @POST("zastoji.php")
    Call<Void> createZastoj(@Body Zastoj zastoj);

    @Multipart
    @POST("zastoji.php")
        //figure this out
    Call<Void> sendZastojWithImages(
            @Part("zastoj") RequestBody zastoj,
            @Part List<MultipartBody.Part> images
    );

    @Multipart
    @POST("remonti.php")
    Call<Void> sendRemontWithImages(
            @Part("remont") RequestBody remont,
            @Part List<MultipartBody.Part> images
    );

    @Headers("Content-Type: application/json")
    @GET("linije.php")
    Call<List<Linija>> getLinije();

    @Headers("Content-Type: application/json")
    @GET("linije.php/{id}")
    Call<Linija> getLinijaById(@Path("id") int id);


    @Headers("Content-Type: application/json")
    @GET("sifrant.php")
    Call<List<Sifrant>> getSifrants();

    @Headers("Content-Type: application/json")
    @GET("sklop_linije.php")
    Call<List<SklopLinije>> getSklopeLinij();

    @Headers("Content-Type: application/json")
    @GET("sifrant.php/{id}")
    Call<Sifrant> getSifrantById(@Path("id") int id);

    @Headers("Content-Type: application/json")
    @PUT("rezervni_deli.php/{id}/adjust")
    Call<Void> adjustStock(@Path("id") int id, @Body StockAdjustmentRequest stockAdjustmentRequest);

    // New endpoints for imenik
    @Headers("Content-Type: application/json")
    @GET("imenik.php")
    Call<List<Imenik>> getImenik();

    @Headers("Content-Type: application/json")
    @POST("imenik.php")
    Call<Void> createImenik(@Body Imenik imenik);

    @Headers("Content-Type: application/json")
    @PUT("imenik.php/{id}")
    Call<Void> updateImenik(@Path("id") int id, @Body Imenik imenik);

    @Headers("Content-Type: application/json")
    @DELETE("imenik.php/{id}")
    Call<Void> deleteImenik(@Path("id") int id);

    @Headers("Content-Type: application/json")
    @GET("imenik.php/{id}")
    Call<Imenik> getImenikById(@Path("id") int id);
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

class StockAdjustmentRequest {
    private int amount;

    public StockAdjustmentRequest(int amount) {
        this.amount = amount;
    }

    // Getter and Setter
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

class ServerResponse {
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
