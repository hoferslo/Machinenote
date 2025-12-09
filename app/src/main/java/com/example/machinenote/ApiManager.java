package com.example.machinenote;

import android.content.Context;
import android.util.Log;

import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.models.DrobniMateriali;
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
import com.example.machinenote.models.Remont;
import com.example.machinenote.models.RezervniDel;
import com.example.machinenote.models.Role;
import com.example.machinenote.models.Sifrant;
import com.example.machinenote.models.SklopLinije;
import com.example.machinenote.models.UpdateResponse;
import com.example.machinenote.models.User;
import com.example.machinenote.models.Zastoj;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiManager {
    private final ApiService apiService;
    private final SharedPreferencesHelper sharedPreferencesHelper;

    public ApiManager(Context context) {
        apiService = ApiClient.getClient().create(ApiService.class);
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
    }

    public ApiManager(Context context, int milliseconds) {
        apiService = ApiClient.getClient(milliseconds).create(ApiService.class);
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
    }

    public void login(String username, String password, LoginCallback callback) {

        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<LoginResponse> call = apiService.login(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String apiKey = loginResponse.getApiKey();
                    Role role = loginResponse.getRole();
                    String locationId = String.valueOf(loginResponse.getLokacija());
                    Log.d("role", String.valueOf(role));
                    ApiClient.setApiKey(apiKey);
                    Log.d("LoginApiManager", "Response body: " + response.body());
                    sharedPreferencesHelper.putString(SharedPreferencesHelper.Token, apiKey);
                    sharedPreferencesHelper.putString(SharedPreferencesHelper.Username, username);
                    sharedPreferencesHelper.putString(SharedPreferencesHelper.Password, password);
                    sharedPreferencesHelper.putString(String.valueOf(SharedPreferencesHelper.LocationID), locationId);
                    Log.d("LoginApiManager", SharedPreferencesHelper.LocationID + ": " + locationId);
                    sharedPreferencesHelper.putRole(role);
                    Log.d("success", "Logged in with user " + username);
                    callback.onSuccess();
                } else {
                    Log.e("error onResponse", String.valueOf(response.body()));
                    callback.onFailure("Login failed");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("error onFailure", String.valueOf(t));
                callback.onFailure(t.getMessage());
            }
        });
    }

    public interface UpdateVersionCallback {
        void onUpdateAvailable(UpdateResponse updateResponse);
        void onNoUpdateNeeded();
        void onFailure(String error);
    }

    public interface UpdateCallback {
        void onSuccess(String message);

        void onSuccess(List<PreventivniPregled> response);
        void onFailure(String errorMessage);
    }

    public void updateOpraviloStatus(int id, PregledOpravilo opravilo, ApiManager.UpdateCallback updateCallback) {

    }

    public interface PreventivniPreglediCallback {
        void onSuccess(List<PreventivniPregled> response);
        void onFailure(String errorMessage);
    }

    public void getPreventivniPregledi(final PreventivniPreglediCallback callback) {
        Call<List<PreventivniPregled>> call = apiService.getPreventivniPregledi();
        call.enqueue(new Callback<List<PreventivniPregled>>() {
            @Override
            public void onResponse(Call<List<PreventivniPregled>> call, Response<List<PreventivniPregled>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to retrieve preventivni pregledi. Response code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("ApiManager", "Error parsing error body", e);
                        }
                    }
                    Log.e("ApiManager", errorMessage);
                    callback.onFailure(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<PreventivniPregled>> call, Throwable t) {
                Log.e("ApiManager", "API call failed for preventivni pregledi: " + t.getMessage(), t);
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void executePreventivniPregled(PreventivniPregled selectedPregled,
                                          int trajanje, String vzdrzevalec,
                                          String datum, String opombe, String dejanskaVrednost,
                                          final PregledExecutionCallback callback) {

        // Ustvari PregledOpravilo objekt z ustreznimi podatki
        PregledOpravilo pregledOpravilo = new PregledOpravilo();

        // Nastavite podatke iz selectedPregled in user input
        pregledOpravilo.setId(selectedPregled.getId()); // To je OpraviloID
        pregledOpravilo.setOpisOpravila(selectedPregled.getOpis());
        pregledOpravilo.setDejanskoTrajanjeMin(trajanje); // Dejansko_Trajanje_Min
        pregledOpravilo.setVzdrzevalec(vzdrzevalec);
        pregledOpravilo.setDatumIzvedbe(datum);
        pregledOpravilo.setOpombe(opombe);
        pregledOpravilo.setActVredParameter(dejanskaVrednost); // Dejanska vrednost
        pregledOpravilo.setStatus("V teku"); // Status med izvajanjem

        // Debug log
        Log.d("ApiManager", "Sending PregledOpravilo: opombe='" + opombe + "', actVredParameter='" + dejanskaVrednost + "'");

        // Dodajte tudi standardne podatke, če so na voljo
        pregledOpravilo.setTrajanjeStdMin(selectedPregled.getTrajanjeStdMin());
        pregledOpravilo.setLastnost(selectedPregled.getLastnost());
        pregledOpravilo.setStdVrednostLastnosti(selectedPregled.getStdVrednost());

        Call<List<PregledOpravilo>> call = apiService.executePreventivniPregled(pregledOpravilo);

        call.enqueue(new Callback<List<PregledOpravilo>>() {
            @Override
            public void onResponse(Call<List<PregledOpravilo>> call, Response<List<PregledOpravilo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to execute preventivni pregled. Response code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("ApiManager", "Error parsing error body", e);
                        }
                    }
                    Log.e("ApiManager", errorMessage);
                    callback.onFailure(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<PregledOpravilo>> call, Throwable t) {
                Log.e("ApiManager", "API call failed for execute preventivni pregled: " + t.getMessage(), t);
                callback.onFailure(t.getMessage());
            }
        });
    }

    // Add this method to your ApiManager class
    public void getOpravilaForPregled(int pregledId, OpravilaCallback callback) {
        Call<List<PregledOpravilo>> call = apiService.getOpravilaForPregled(pregledId);
        call.enqueue(new Callback<List<PregledOpravilo>>() {
            @Override
            public void onResponse(Call<List<PregledOpravilo>> call, Response<List<PregledOpravilo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PregledOpravilo> opravilaList = response.body();
                    Log.d("ApiManager", "Opravila retrieved successfully for pregled: " + pregledId);
                    callback.onSuccess(opravilaList);
                } else {
                    String errorMessage = "Failed to retrieve opravila for pregled " + pregledId + ". Response code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("ApiManager", "Error parsing error body", e);
                        }
                    }
                    Log.e("ApiManager", errorMessage);
                    callback.onFailure(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<PregledOpravilo>> call, Throwable t) {
                Log.e("ApiManager", "API call failed for opravila: " + t.getMessage(), t);
                callback.onFailure(t.getMessage());
            }
        });
    }

    // Add the callback interface at the bottom of your ApiManager class with the other interfaces
    public interface OpravilaCallback {
        void onSuccess(List<PregledOpravilo> opravilaList);
        void onFailure(String errorMessage);
    }

    public void getZastoji(ZastojiCallback callback) {
        Call<List<Zastoj>> call = apiService.getZastoji();
        call.enqueue(new Callback<List<Zastoj>>() {
            @Override
            public void onResponse(Call<List<Zastoj>> call, Response<List<Zastoj>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Zastoj> zastojiList = response.body();
                    callback.onSuccess(zastojiList);
                } else {
                    callback.onFailure("Failed to retrieve zastoji");
                }
            }

            @Override
            public void onFailure(Call<List<Zastoj>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void sendZastoj(Zastoj zastoj, final Callback<Void> callback) {
        Call<Void> call = apiService.createZastoj(zastoj);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ZastojManager", "Zastoj sent successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("ZastojManager", "Failed to send Zastoj: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ZastojManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void sendZastojWithImages(Zastoj zastoj, List<File> imageFiles, final Callback<Void> callback) {
        // Convert Zastoj to RequestBody
        RequestBody zastojBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(zastoj));

        // Convert image files to MultipartBody.Part
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (File file : imageFiles) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("images[]", file.getName(), requestFile);
            imageParts.add(body);
        }

        // Call the API
        Call<Void> call = apiService.sendZastojWithImages(zastojBody, imageParts);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ApiManager", "Zastoj and images sent successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("ApiManager", "Failed to send Zastoj and images: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ApiManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void sendRemontWithImages(Remont remont, List<File> imageFiles, final Callback<Void> callback) {
        // Convert Zastoj to RequestBody
        RequestBody remontBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(remont));

        // Convert image files to MultipartBody.Part
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (File file : imageFiles) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("images[]", file.getName(), requestFile);
            imageParts.add(body);
        }

        // Call the API
        Call<Void> call = apiService.sendRemontWithImages(remontBody, imageParts);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ApiManager", "Remont and images sent successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("ApiManager", "Failed to send Remont and images: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ApiManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void getRezervniDeli(RezervniDeliCallback callback) {
        Call<List<RezervniDel>> call = apiService.getRezervniDel();
        call.enqueue(new Callback<List<RezervniDel>>() {
            @Override
            public void onResponse(Call<List<RezervniDel>> call, Response<List<RezervniDel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RezervniDel> rezervniDeliList = response.body();
                    Log.d("ApiManager", "Rezervni Deli retrieved successfully");
                    Log.d("ApiManager", "Rezervni Deli list: " + rezervniDeliList);
                    callback.onSuccess(rezervniDeliList);
                } else {
                    callback.onFailure("Failed to retrieve rezervni deli");
                }
            }

            @Override
            public void onFailure(Call<List<RezervniDel>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void fetchKemikalijaId(KemikalijaIdCallback callback, int id) {
        Call<Kemikalija> call = apiService.getKemikalijaById(id);
        call.enqueue(new Callback<Kemikalija>() {
            @Override
            public void onResponse(Call<Kemikalija> call, Response<Kemikalija> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Kemikalija kemikalija = response.body();
                    Log.d("ApiManager", "Kemikalija retrieved successfully");
                    Log.d("ApiManager", "Kemikalija list: " + kemikalija);
                    callback.onSuccess(kemikalija);
                } else {
                    callback.onFailure("Failed to retrieve Kemikalije");
                }
            }

            @Override
            public void onFailure(Call<Kemikalija> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void fetchAllKemikalije(KemikalijeListCallback callback) {
        Call<List<Kemikalija>> call = apiService.getKemikalije();
        call.enqueue(new Callback<List<Kemikalija>>() {
            @Override
            public void onResponse(Call<List<Kemikalija>>  call, Response<List<Kemikalija>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Kemikalija> kemikalija = response.body();
                    Log.d("ApiManager", "Kemikalija retrieved successfully");
                    Log.d("ApiManager", "Kemikalija list: " + kemikalija);
                    callback.onSuccess(kemikalija);
                } else {
                    callback.onFailure("Failed to retrieve Kemikalije");
                }
            }

            @Override
            public void onFailure(Call<List<Kemikalija>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void createKemikalija(Kemikalija kemikalija, final Callback<Void> callback) {
        Call<Void> call = apiService.createKemikalija(kemikalija);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Kemikalija", "Kemikalija created successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("Kemikalija", "Failed to create Kemikalija: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Kemikalija", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    // Fetch all Police (Shelves)
    public void fetchAllPoliceKemikalije(final PoliceListCallback callback) {
        Call<List<PolicaKemikalije>> call = apiService.fetchAllPoliceKemikalije();
        call.enqueue(new Callback<List<PolicaKemikalije>>() {
            @Override
            public void onResponse(Call<List<PolicaKemikalije>> call, Response<List<PolicaKemikalije>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PolicaKemikalije> police = response.body();
                    Log.d("ApiManager", "Fetched " + police.size() + " police");
                    callback.onSuccess(police);
                } else {
                    String errorMsg = "Failed to fetch police: " + response.message();
                    Log.e("ApiManager", errorMsg);
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<PolicaKemikalije>> call, Throwable t) {
                String errorMsg = "Error fetching police: " + t.getMessage();
                Log.e("ApiManager", errorMsg, t);
                callback.onFailure(errorMsg);
            }
        });
    }

    // Fetch all Omare (Cabinets)
    public void fetchAllOmareKemikalije(final OmareListCallback callback) {
        Call<List<OmaraKemikalije>> call = apiService.fetchAllOmareKemikalije();
        call.enqueue(new Callback<List<OmaraKemikalije>>() {
            @Override
            public void onResponse(Call<List<OmaraKemikalije>> call, Response<List<OmaraKemikalije>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OmaraKemikalije> omare = response.body();
                    Log.d("ApiManager", "Fetched " + omare.size() + " omare");
                    callback.onSuccess(omare);
                } else {
                    String errorMsg = "Failed to fetch omare: " + response.message();
                    Log.e("ApiManager", errorMsg);
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<OmaraKemikalije>> call, Throwable t) {
                String errorMsg = "Error fetching omare: " + t.getMessage();
                Log.e("ApiManager", errorMsg, t);
                callback.onFailure(errorMsg);
            }
        });
    }

    public void updateUserLocation(final int lokacija_id, final String username, final Callback<Void> callback){
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);  // String
        body.put("lokacija_id", lokacija_id);  // Integer

        Call<Void> call = apiService.updateUserLocation(body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response< Void> response) {
                if (response.isSuccessful()) {
                    Log.d("updateUser", "User location updated successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("updateUser", "Failed to update User location: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("updateUser", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }


    public void createRezervniDeli(RezervniDel rezervniDel, final Callback<Void> callback) {
        Call<Void> call = apiService.createRezervniDeli(rezervniDel);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("RezervniDeliManager", "Rezervni Deli created successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("RezervniDeliManager", "Failed to create Rezervni Deli: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("RezervniDeliManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void updateRezervniDeli(int id, RezervniDel rezervniDel, final Callback<Void> callback) {
        Call<Void> call = apiService.updateRezervniDel(id, rezervniDel);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("RezervniDeliManager", "Rezervni Deli updated successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("RezervniDeliManager", "Failed to update Rezervni Deli: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("RezervniDeliManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void fetchRezervniDeliById(int id, RezervniDeliByIdCallback callback) {
        Call<RezervniDel> call = apiService.getRezervniDelById(id);

        call.enqueue(new Callback<RezervniDel>() {
            @Override
            public void onResponse(Call<RezervniDel> call, Response<RezervniDel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to fetch rezervni deli");
                }
            }

            @Override
            public void onFailure(Call<RezervniDel> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void fetchLinije(LinijeCallback callback) {
        Call<List<Linija>> call = apiService.getLinije("linije");

        call.enqueue(new Callback<List<Linija>>() {
            @Override
            public void onResponse(Call<List<Linija>> call, Response<List<Linija>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    List<Linija> linije = response.body();
                    callback.onSuccess(linije);
                } else {
                    // Log error response body
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("API_ERROR", "Error response body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e("API_ERROR", "Could not read error body: " + e.getMessage());
                    }
                    String errorMsg = "Failed to fetch linije: " + response.message();
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Linija>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                callback.onFailure(errorMsg);
            }
        });
    }


    public void fetchLinijaById(int id, LinijaCallback callback) {
        Call<Linija> call = apiService.getLinijaById("linije", id);

        call.enqueue(new Callback<Linija>() {
            @Override
            public void onResponse(Call<Linija> call, Response<Linija> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to fetch linija");
                }
            }

            @Override
            public void onFailure(Call<Linija> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void fetchSifrants(SifrantCallback callback) {
        Call<List<Sifrant>> call = apiService.getSifrants();

        call.enqueue(new Callback<List<Sifrant>>() {
            @Override
            public void onResponse(Call<List<Sifrant>> call, Response<List<Sifrant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to fetch sifrants");
                }
            }

            @Override
            public void onFailure(Call<List<Sifrant>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void fetchSklopeLinij(SklopLinijeCallback callback) {
        Call<List<SklopLinije>> call = apiService.getSklopeLinij();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<SklopLinije>> call, Response<List<SklopLinije>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to fetch sklope linij");
                }
            }

            @Override
            public void onFailure(Call<List<SklopLinije>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void fetchLokacije(LokacijeCallback callback) {
        Call<List<Lokacija>> call = apiService.getLokacije("lokacije");

        call.enqueue(new Callback<List<Lokacija>>() {
            @Override
            public void onResponse(Call<List<Lokacija>> call, Response<List<Lokacija>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to fetch sklope linij");
                }
            }

            @Override
            public void onFailure(Call<List<Lokacija>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    // Fetch sifrant by ID
    public void fetchSifrantById(int id, SifrantByIdCallback callback) {
        Call<Sifrant> call = apiService.getSifrantById(id);

        call.enqueue(new Callback<Sifrant>() {
            @Override
            public void onResponse(Call<Sifrant> call, Response<Sifrant> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to fetch sifrant");
                }
            }

            @Override
            public void onFailure(Call<Sifrant> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void checkServerConnection(final ConnectionCallback callback) {
        Call<ServerResponse> call = apiService.checkConnection(); // Define this endpoint in ApiService
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null && "ok".equals(serverResponse.getStatus())) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Unexpected status: " + (serverResponse != null ? serverResponse.getStatus() : "null"));
                    }
                } else {
                    callback.onFailure("Connection failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void adjustStock(int id, int amount, final StockAdjustmentCallback callback) {
        StockAdjustmentRequest request = new StockAdjustmentRequest(amount);
        Call<Void> call = apiService.adjustStock(id, request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ApiManager", "Stock adjusted successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("ApiManager", "Failed to adjust stock: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ApiManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void getImenik(ImenikCallback callback) {
        Call<List<Imenik>> call = apiService.getImenik();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Imenik>> call, Response<List<Imenik>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Imenik> imenikList = response.body();
                    callback.onSuccess(imenikList);
                } else {
                    callback.onFailure("Failed to retrieve imenik");
                }
            }

            @Override
            public void onFailure(Call<List<Imenik>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void createImenik(Imenik imenik, final Callback<Void> callback) {
        Call<Void> call = apiService.createImenik(imenik);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ImenikManager", "Imenik created successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("ImenikManager", "Failed to create Imenik: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ImenikManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void updateImenik(int id, Imenik imenik, final Callback<Void> callback) {
        Call<Void> call = apiService.updateImenik(id, imenik);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ImenikManager", "Imenik updated successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("ImenikManager", "Failed to update Imenik: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ImenikManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void deleteImenik(int id, final Callback<Void> callback) {
        Call<Void> call = apiService.deleteImenik(id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ImenikManager", "Imenik deleted successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("ImenikManager", "Failed to delete Imenik: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ImenikManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void getImenikById(int id, final ImenikCallback callback) {
        Call<Imenik> call = apiService.getImenikById(id);
        call.enqueue(new Callback<Imenik>() {
            @Override
            public void onResponse(Call<Imenik> call, Response<Imenik> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Imenik imenik = response.body();
                    callback.onSuccess(List.of(imenik));
                } else {
                    callback.onFailure("Failed to retrieve imenik");
                }
            }

            @Override
            public void onFailure(Call<Imenik> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void getNaloge(NalogaCallback callback) {
        Call<List<Naloga>> call = apiService.getNaloge();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Naloga>> call, Response<List<Naloga>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Naloga> nalogaList = response.body();
                    callback.onSuccess(nalogaList);
                } else {
                    callback.onFailure("Failed to retrieve naloge");
                }
            }

            @Override
            public void onFailure(Call<List<Naloga>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void sendNalogaWithImages(Naloga naloga, List<File> imageFiles, final Callback<Void> callback) {
        // Convert Naloga to RequestBody
        RequestBody nalogaBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(naloga));

        // Convert image files to MultipartBody.Part
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (File file : imageFiles) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("images[]", file.getName(), requestFile);  // Changed "images" to "images[]"
            imageParts.add(body);
        }

        // Call the API
        Call<Void> call = apiService.sendNalogaWithImages(nalogaBody, imageParts);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ApiManager", "Naloga and images sent successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("ApiManager", "Failed to send Naloga and images: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ApiManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void updateNalogaWithImages(int id, Naloga naloga, List<File> imageFiles, final Callback<Void> callback) {
        // Convert Naloga to RequestBody
        RequestBody nalogaBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(naloga));

        // Convert image files to MultipartBody.Part
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (File file : imageFiles) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("completion_images[]", file.getName(), requestFile);
            imageParts.add(body);
        }

        // Call the API
        Call<Void> call = apiService.updateNalogaWithImages(id, nalogaBody, imageParts);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ApiManager", "Naloga updated with images successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("ApiManager", "Failed to update Naloga with images: " + response.message());

                    // Dodatno logging za debugging
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("ApiManager", "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("ApiManager", "Could not read error body");
                    }

                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ApiManager", "Error updating naloga with images: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void createUser(RegistrationRequest registrationRequest, RegistrationCallback callback) {
        // Step 3: Create user with the role_id
        int roleId = 31;
        if(!((registrationRequest.getRole()).equals("Gost"))){
            return;
        }
        UserCreationRequest userRequest = new UserCreationRequest(registrationRequest.getUsername(), registrationRequest.getPassword(), roleId);
        Call<RegistrationResponse> createUserCall = apiService.createUser(userRequest);

        createUserCall.enqueue(new Callback<RegistrationResponse>() {
            @Override
            public void onResponse(Call<RegistrationResponse> call, Response<RegistrationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegistrationResponse registrationResponse = response.body();
                    if (registrationResponse.isSuccess()) {
                        Log.d("ApiManager", "User registered successfully: " + registrationRequest.getUsername());
                        callback.onSuccess();
                    } else {
                        Log.e("ApiManager", "User creation failed: " + registrationResponse.getMessage());
                        callback.onFailure("User creation failed: " + registrationResponse.getMessage());
                    }
                } else {
                    Log.e("ApiManager", "User creation failed: " + response.message());
                    callback.onFailure("User creation failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RegistrationResponse> call, Throwable t) {
                Log.e("ApiManager", "User creation error: " + t.getMessage());
                callback.onFailure("User creation error: " + t.getMessage());
            }
        });
    }

    public void getRoles(Callback<List<Role>> callback) {
        Call<List<Role>> call = apiService.getRoles();
        call.enqueue(new Callback<List<Role>>() {
            @Override
            public void onResponse(Call<List<Role>> call, Response<List<Role>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Role> roles = response.body();
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, new Throwable("Failed to retrieve roles: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<List<Role>> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void createRole(String roleName, List<String> permissions, RoleCreationCallback callback) {
        RoleRequest roleRequest = new RoleRequest(roleName, permissions);
        Call<RoleResponse> createRoleCall = apiService.createRole(roleRequest);

        createRoleCall.enqueue(new Callback<RoleResponse>() {
            @Override
            public void onResponse(Call<RoleResponse> call, Response<RoleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RoleResponse roleResponse = response.body();
                    if (roleResponse.isSuccess()) {
                        Log.d("ApiManager", "Role created successfully: " + roleName);
                        callback.onSuccess();
                    } else {
                        Log.e("ApiManager", "Role creation failed: " + roleResponse.getMessage());
                        callback.onFailure("Role creation failed: " + roleResponse.getMessage());
                    }
                } else {
                    Log.e("ApiManager", "Role creation failed: " + response.message());
                    callback.onFailure("Role creation failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RoleResponse> call, Throwable t) {
                Log.e("ApiManager", "Role creation error: " + t.getMessage());
                callback.onFailure("Role creation error: " + t.getMessage());
            }
        });
    }

    public void getUsers(Callback<List<User>> callback) {
        Call<List<User>> call = apiService.getUsers();
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, new Throwable("Failed to retrieve users: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    /**
     * Update existing user TODO: NEED TO ADD THIS TO PHP
     */
    public void updateUser(String username, User user, Callback<User> callback) {
        Call<User> call = apiService.updateUser(username, user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, new Throwable("Failed to update user: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    /**
     * Delete user TODO: NEED TO ADD THIS TO PHP
     */
    public void deleteUser(String username, Callback<Void> callback) {
        Call<Void> call = apiService.deleteUser(username);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, new Throwable("Failed to delete user: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    /**
     * Update existing role TODO: NEED TO ADD THIS TO PHP
     */
    public void updateRole(int roleId, Role role, Callback<Role> callback) {
        Call<Role> call = apiService.updateRole(roleId, role);
        call.enqueue(new Callback<Role>() {
            @Override
            public void onResponse(Call<Role> call, Response<Role> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, new Throwable("Failed to update role: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<Role> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }
    /**
     * Delete existing role TODO: NEED TO ADD THIS TO PHP
     */

    public void deleteRole(int roleId, Callback<Void> callback) {
        Call<Void> call = apiService.deleteRole(roleId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, new Throwable("Failed to delete role: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void getDrobniMateriali(DrobniMaterialiCallback callback) {
        Call<List<DrobniMateriali>> call = apiService.getDrobniMateriali();
        call.enqueue(new Callback<List<DrobniMateriali>>() {
            @Override
            public void onResponse(Call<List<DrobniMateriali>> call, Response<List<DrobniMateriali>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ApiManager", "Drobni materiale retrieved successfully");
                    List<DrobniMateriali> drobniMaterialiList = response.body();
                    callback.onSuccess(drobniMaterialiList);
                } else {
                    callback.onFailure("Failed to retrieve rezervni deli");
                }
            }

            @Override
            public void onFailure(Call<List<DrobniMateriali>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void checkForUpdate(String currentVersion, String packageName, UpdateVersionCallback callback) {
        UpdateRequest request = new UpdateRequest(currentVersion, packageName);
        Call<UpdateResponse> call = apiService.checkForUpdate(request);

        call.enqueue(new Callback<UpdateResponse>() {
            @Override
            public void onResponse(Call<UpdateResponse> call, Response<UpdateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UpdateResponse updateResponse = response.body();
                    if (updateResponse.isUpdateAvailable()) {
                        callback.onUpdateAvailable(updateResponse);
                    } else {
                        callback.onNoUpdateNeeded();
                    }
                } else {
                    callback.onFailure("Failed to check for updates");
                }
            }

            @Override
            public void onFailure(Call<UpdateResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void getNarocila(NarocilaCallback callback) {
        Call<List<Narocila>> call = apiService.getNarocilaApi();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Narocila>> call, Response<List<Narocila>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Narocila> narocilaList = response.body();
                    callback.onSuccess(narocilaList);
                } else {
                    callback.onFailure("Failed to retrieve naloge");
                }
            }

            @Override
            public void onFailure(Call<List<Narocila>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void sendNarocilaWithImages(Narocila narocila, List<File> imageFiles, final Callback<Void> callback) {
        // Convert Naloga to RequestBody
        RequestBody narocilaBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(narocila));

        // Convert image files to MultipartBody.Part
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (File file : imageFiles) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("images[]", file.getName(), requestFile);  // Changed "images" to "images[]"
            imageParts.add(body);
        }

        // Call the API
        Call<Void> call = apiService.sendNarocilaWithImages(narocilaBody, imageParts);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ApiManager", "Narocila and images sent successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("ApiManager", "Failed to send Narocila and images: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ApiManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void updateNarocilaWithImages(int id, Narocila narocila, List<File> imageFiles, final Callback<Void> callback) {
        // Convert Naloga to RequestBody
        RequestBody narocilaBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(narocila));

        // Convert image files to MultipartBody.Part
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        if (imageFiles != null) {
            for (File file : imageFiles) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("completion_images[]", file.getName(), requestFile);
                imageParts.add(body);
            }
        }

        // Call the API
        Call<Void> call = apiService.updateNarocilaWithImages(id, narocilaBody, imageParts);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ApiManager", "Narocila updated with images successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("ApiManager", "Failed to update Narocila with images: " + response.message());

                    // Dodatno logging za debugging
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("ApiManager", "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("ApiManager", "Could not read error body");
                    }

                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ApiManager", "Error updating narocila with images: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }


    public interface PoliceListCallback {
        void onSuccess(List<PolicaKemikalije> police);
        void onFailure(String errorMessage);
    }

    public interface OmareListCallback {
        void onSuccess(List<OmaraKemikalije> omare);
        void onFailure(String errorMessage);
    }

    public interface KemikalijeListCallback{
        void onSuccess(List<Kemikalija> response);
        void onFailure(String errorMessage);
    }

    public interface KemikalijaIdCallback{
        void onSuccess(Kemikalija response);
        void onFailure(String errorMessage);
    }

    // Callback interface
    public interface PregledExecutionCallback {
        void onSuccess(List<PregledOpravilo> response);
        void onFailure(String errorMessage);
    }

    public interface NarocilaCallback {
        void onSuccess(List<Narocila> narocilaList);

        void onFailure(String message);
    }

    public interface RoleCreationCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    // Add this callback interface to your ApiManager class
    public interface RegistrationCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    // Additional methods for other endpoints (e.g., Linije, Sifrant) would follow a similar pattern

    public interface ImenikCallback {
        void onSuccess(List<Imenik> imenikList);

        void onFailure(String message);
    }

    public interface NalogaCallback {
        void onSuccess(List<Naloga> nalogaList);

        void onFailure(String message);
    }

    // Callback interface for connection checking
    public interface ConnectionCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    public interface LinijeCallback {
        void onSuccess(List<Linija> linije);

        void onFailure(String errorMessage);
    }

    public interface LinijaCallback {
        void onSuccess(Linija linija);

        void onFailure(String errorMessage);
    }

    public interface LoginCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    public interface ZastojiCallback {
        void onSuccess(List<Zastoj> zastoji);

        void onFailure(String errorMessage);
    }


    // SifrantCallback.java
    public interface SifrantCallback {
        void onSuccess(List<Sifrant> sifrants);

        void onFailure(String errorMessage);
    }

    public interface SklopLinijeCallback {
        void onSuccess(List<SklopLinije> sklopiLinij);

        void onFailure(String errorMessage);
    }

    public interface LokacijeCallback {
        void onSuccess(List<Lokacija> lokacija);

        void onFailure(String errorMessage);
    }

    // SifrantByIdCallback.java
    public interface SifrantByIdCallback {
        void onSuccess(Sifrant sifrant);

        void onFailure(String errorMessage);
    }

    public interface RezervniDeliCallback {
        void onSuccess(List<RezervniDel> rezervniDeliList);

        void onFailure(String errorMessage);
    }

    public interface DrobniMaterialiCallback {
        void onSuccess(List<DrobniMateriali> drobniMaterialiList);

        void onFailure(String errorMessage);
    }

    public interface RezervniDeliByIdCallback {
        void onSuccess(RezervniDel rezervniDel);

        void onFailure(String errorMessage);
    }

    // Define callback interface for stock adjustment
    public interface StockAdjustmentCallback {

        void onFailure(Call<Void> call, Throwable throwable);

        void onResponse(Call<Void> call, Response<Void> response);
    }



}
