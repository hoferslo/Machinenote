package com.example.machinenote;

import com.example.machinenote.models.DrobniMateriali;
import com.example.machinenote.models.Imenik;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.Naloga;
import com.example.machinenote.models.PregledOpravilo;
import com.example.machinenote.models.PreventivniPregled;
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
    @GET("preventivni_pregledi.php") // Predvidevana pot do vaše PHP skripte
    Call<List<PreventivniPregled>> getPreventivniPregledi();

    @Headers("Content-Type: application/json")
    @GET("opravila.php/{pregledId}")
    Call<List<PregledOpravilo>> getOpravilaForPregled(@Path("pregledId") int pregledId);

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

    // New endpoints for naloge
    @Headers("Content-Type: application/json")
    @GET("naloge.php")
    Call<List<Naloga>> getNaloge();

    @Multipart
    @POST("naloge.php")
    Call<Void> sendNalogaWithImages(
            @Part("naloga") RequestBody naloga,  // Changed from "naloge" to "naloga"
            @Part List<MultipartBody.Part> images
    );

    @Headers("Content-Type: application/json")
    @PUT("naloge.php/{id}")
    Call<Void> updateNaloga(@Path("id") int id, @Body Naloga naloga);

    @Headers("Content-Type: application/json")
    @GET("roles.php")
    Call<List<Role>> getRoles();

    // Create a new role
    @Headers("Content-Type: application/json")
    @POST("roles.php")
    Call<RoleResponse> createRole(@Body RoleRequest roleRequest);

    // Create a new user (modify existing createUser to accept registration data)
    @Headers("Content-Type: application/json")
    @POST("users.php")
    Call<RegistrationResponse> createUser(@Body UserCreationRequest userRequest);

    @Headers("Content-Type: application/json")
    @GET("drobni_materiali.php")
    Call<List<DrobniMateriali>> getDrobniMateriali();
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

class RegistrationResponse {
    private boolean success;
    private String message;
    private int userId;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}

class RoleRequest {
    private String role;
    private boolean knjizenje;
    private boolean rezervniDeli;
    private boolean imenik;
    private boolean preventivniPregledi;
    private boolean zastoji;
    private boolean naloge;
    private boolean dodajanjeNalog;
    private boolean remonti;
    private boolean orodja;
    private boolean register;

    public RoleRequest(String role, List<String> permissions) {
        this.role = role;
        this.knjizenje = permissions.contains("Knjiženje");
        this.rezervniDeli = permissions.contains("Rezervni deli");
        this.imenik = permissions.contains("Imenik");
        this.preventivniPregledi = permissions.contains("Preventivni pregledi");
        this.zastoji = permissions.contains("Zastoji");
        this.naloge = permissions.contains("Naloge");
        this.dodajanjeNalog = permissions.contains("Dodajanje nalog");
        this.remonti = permissions.contains("Remonti");
        this.orodja = permissions.contains("Orodja");
        this.register = permissions.contains("Register");
    }

    // Getters and setters...
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isKnjizenje() { return knjizenje; }
    public void setKnjizenje(boolean knjizenje) { this.knjizenje = knjizenje; }
    public boolean isRezervni_deli() { return rezervniDeli; }
    public void setRezervni_deli(boolean rezervniDeli) { this.rezervniDeli = rezervniDeli; }
    public boolean isImenik() { return imenik; }
    public void setImenik(boolean imenik) { this.imenik = imenik; }
    public boolean isPreventivni_pregledi() { return preventivniPregledi; }
    public void setPreventivni_pregledi(boolean preventivni_pregledi) { this.preventivniPregledi = preventivniPregledi; }
    public boolean isZastoji() { return zastoji; }
    public void setZastoji(boolean zastoji) { this.zastoji = zastoji; }
    public boolean isNaloge() { return naloge; }
    public void setNaloge(boolean naloge) { this.naloge = naloge; }
    public boolean isDodajanje_nalog() { return dodajanjeNalog; }
    public void setDodajanje_nalog(boolean dodajanje_nalog) { this.dodajanjeNalog = dodajanje_nalog; }
    public boolean isRemonti() { return remonti; }
    public void setRemonti(boolean remonti) { this.remonti = remonti; }
    public boolean isOrodja() { return orodja; }
    public void setOrodja(boolean orodja) { this.orodja = orodja; }
    public boolean isRegister() { return register; }
    public void setRegister(boolean register) { this.register = register; }
}

class UserCreationRequest {
    private String username;
    private String password;
    private int role_id;

    public UserCreationRequest(String username, String password, int roleId) {
        this.username = username;
        this.password = password;
        this.role_id = roleId;
    }

    // Getters and setters...
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getRole_id() { return role_id; }
    public void setRole_id(int role_id) { this.role_id = role_id; }
}

class RoleResponse {
    private boolean success;
    private String message;
    private int role_id;

    // Getters and setters...
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getRole_id() { return role_id; }
    public void setRole_id(int role_id) { this.role_id = role_id; }
}