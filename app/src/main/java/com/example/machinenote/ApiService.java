package com.example.machinenote;

import com.example.machinenote.models.DrobniMateriali;
import com.example.machinenote.models.Enota;
import com.example.machinenote.models.Imenik;
import com.example.machinenote.models.Kemikalija;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.Lokacija;
import com.example.machinenote.models.Naloga;
import com.example.machinenote.models.Narocila;
import com.example.machinenote.models.OmaraKemikalije;
import com.example.machinenote.models.PolicaKemikalije;
import com.example.machinenote.models.PregledOpravilo;
import com.example.machinenote.models.PreventivniPregled;
import com.example.machinenote.models.RezervniDel;
import com.example.machinenote.models.Role;
import com.example.machinenote.models.Sifrant;
import com.example.machinenote.models.SklopLinije;
import com.example.machinenote.models.UpdateResponse;
import com.example.machinenote.models.User;
import com.example.machinenote.models.Zastoj;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

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
import retrofit2.http.Query;

public interface ApiService {

    @GET("connection.php")
    Call<ServerResponse> checkConnection();

    @Headers("Content-Type: application/json")
    @POST("users.php/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @Headers("Content-Type: application/json")
    @GET("users.php")
    Call<List<User>> getUsers();

    @Headers("Content-Type: application/json")
    @PUT("users.php/{username}")
    Call<User> updateUser(@Path("username") String username, @Body User user);

    @Headers("Content-Type: application/json")
    @GET("preventivni_pregledi.php")
    Call<List<PreventivniPregled>> getPreventivniPregledi();

    @Headers("Content-Type: application/json")
    @GET("rezervni_deli.php")
    Call<List<RezervniDel>> getRezervniDel();

    @Headers("Content-Type: application/json")
    @GET("Razvoj/kemikalije.php")
    Call<List<Kemikalija>> getKemikalije();

    @Headers("Content-Type: application/json")
    @GET("Razvoj/kemikalije.php/{id}")
    Call<Kemikalija> getKemikalijaById(@Path("id") int id);

    @Headers("Content-Type: application/json")
    @POST("Razvoj/kemikalije.php")
    Call<Void> createKemikalija(@Body Kemikalija kemikalija);

    @Headers("Content-Type: application/json")
    @PUT("Razvoj/kemikalije.php")
    Call<Void> updateKemikalija(@Body Kemikalija kemikalija);

    @Headers("Content-Type: application/json")
    @GET("Razvoj/polica.php")
    Call<List<PolicaKemikalije>> fetchAllPoliceKemikalije();

    @Headers("Content-Type: application/json")
    @GET("Razvoj/omara.php")
    Call<List<OmaraKemikalije>> fetchAllOmareKemikalije();


    @Headers("Content-Type: application/json")
    @GET("rezervni_deli.php/{id}")
    Call<RezervniDel> getRezervniDelById(@Path("id") int id);


    @Multipart
    @POST("zastoji.php")
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
    @GET("helpers.php")
    Call<List<Linija>> getLinije(@Query("action") String action);

    @Headers("Content-Type: application/json")
    @GET("sklop_linije.php")
    Call<List<SklopLinije>> getSklopeLinij();

    @Headers("Content-Type: application/json")
    @GET("sifrant.php")
    Call<List<Sifrant>> getSifrants();

    @Headers("Content-Type: application/json")
    @GET("helpers.php")
    Call<List<Enota>> getEnote(@Query("action") String action);

    @Headers("Content-Type: application/json")
    @PUT("rezervni_deli.php/{id}/adjust")
    Call<Void> adjustStock(@Path("id") int id, @Body StockAdjustmentRequest stockAdjustmentRequest);

    @Headers("Content-Type: application/json")
    @GET("imenik.php")
    Call<List<Imenik>> getImenik();

    @Headers("Content-Type: application/json")
    @GET("naloge.php")
    Call<List<Naloga>> getNaloge();

    @Multipart
    @POST("naloge.php")
    Call<Void> sendNalogaWithImages(
            @Part("naloga") RequestBody naloga,
            @Part List<MultipartBody.Part> images
    );

    @POST("naloge.php")
    @Multipart
    Call<Void> updateNalogaWithImages(
            @Query("id") int id,
            @Part("naloga") RequestBody naloga,
            @Part List<MultipartBody.Part> completionImages
    );

    @Headers("Content-Type: application/json")
    @GET("narocila.php")
    Call<List<Narocila>> getNarocilaApi();

    @Multipart
    @POST("narocila.php")
    Call<Void> sendNarocilaWithImages(
            @Part("narocila") RequestBody narocila,
            @Part List<MultipartBody.Part> images
    );

    @POST("narocila.php")
    @Multipart
    Call<Void> updateNarocilaWithImages(
            @Query("id") int id,
            @Part("narocila") RequestBody narocila,
            @Part List<MultipartBody.Part> completionImages
    );

    @Headers("Content-Type: application/json")
    @GET("roles.php")
    Call<List<Role>> getRoles();

    @Headers("Content-Type: application/json")
    @POST("roles.php")
    Call<RoleResponse> createRole(@Body RoleRequest roleRequest);

    @Headers("Content-Type: application/json")
    @POST("users.php/register")
    Call<RegistrationResponse> createUser(@Body UserCreationRequest userRequest);

    @Headers("Content-Type: application/json")
    @PUT("users.php/location")
    Call<Void> updateUserLocation(@Body Map<String, Object> body);


    @Headers("Content-Type: application/json")
    @GET("drobni_materiali.php")
    Call<List<DrobniMateriali>> getDrobniMateriali();

    @Headers("Content-Type: application/json")
    @POST("check-update.php")
    Call<UpdateResponse> checkForUpdate(@Body UpdateRequest request);

    @Headers("Content-Type: application/json")
    @GET("helpers.php")
    Call<List<Lokacija>> getLokacije(@Query("action") String action);

    @Headers("Content-Type: application/json")
    @POST("preventivni_pregledi.php?action=create_izvedba")
    Call<List<PregledOpravilo>> executePreventivniPregled(@Body PregledOpravilo pregledOpravilo);
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
    private int lokacija_id;

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

    public int getLokacija() { return lokacija_id; }
}

class StockAdjustmentRequest {
    private double amount;

    public StockAdjustmentRequest(double amount) {
        this.amount = amount;
    }

    // Getter and Setter
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
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
    private boolean narocila;
    private boolean dodajanje_narocil;
    private boolean upravljanje_narocil;
    private boolean kemikalije;

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
        this.narocila = permissions.contains("Naročila");
        this.dodajanje_narocil = permissions.contains("Dodajanje Naročil");
        this.upravljanje_narocil = permissions.contains("Upravljanje Naročil");
        this.kemikalije = permissions.contains("Kemikalije");
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
    public boolean isNarocila() { return narocila; }
    public void setNarocila(boolean narocila) { this.narocila = narocila; }
    public boolean isDodajanje_narocil() { return dodajanje_narocil; }
    public void setDodajanje_narocil(boolean dodajanje_narocil) { this.dodajanje_narocil = dodajanje_narocil; }
    public boolean isUpravljanje_narocil() { return upravljanje_narocil; }
    public void setUpravljanje_narocil(boolean upravljanje_narocil) { this.upravljanje_narocil = upravljanje_narocil; }
    public boolean isKemikalije() { return kemikalije; }
    public void setKemikalije(boolean kemikalije) { this.kemikalije = kemikalije; }
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

class UpdateRequest {
    @SerializedName("current_version")
    private String currentVersion;

    @SerializedName("package_name")
    private String packageName;

    public UpdateRequest(String currentVersion, String packageName) {
        this.currentVersion = currentVersion;
        this.packageName = packageName;
    }

    // Getters
    public String getCurrentVersion() { return currentVersion; }
    public String getPackageName() { return packageName; }
}

