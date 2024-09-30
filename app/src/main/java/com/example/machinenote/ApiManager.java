package com.example.machinenote;

import android.content.Context;
import android.util.Log;

import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.models.Imenik;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.Naloga;
import com.example.machinenote.models.Remont;
import com.example.machinenote.models.RezervniDel;
import com.example.machinenote.models.Role;
import com.example.machinenote.models.Sifrant;
import com.example.machinenote.models.SklopLinije;
import com.example.machinenote.models.Zastoj;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
                    ApiClient.setApiKey(apiKey);
                    sharedPreferencesHelper.putString(SharedPreferencesHelper.Token, apiKey);
                    sharedPreferencesHelper.putString(SharedPreferencesHelper.Username, username);
                    sharedPreferencesHelper.putString(SharedPreferencesHelper.Password, password);
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
        Call<List<Linija>> call = apiService.getLinije();

        call.enqueue(new Callback<List<Linija>>() {
            @Override
            public void onResponse(Call<List<Linija>> call, Response<List<Linija>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to fetch linije");
                }
            }

            @Override
            public void onFailure(Call<List<Linija>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void fetchLinijaById(int id, LinijaCallback callback) {
        Call<Linija> call = apiService.getLinijaById(id);

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

    public void createNaloga(Naloga naloga, final Callback<Void> callback) {
        Call<Void> call = apiService.createNaloga(naloga);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("NalogaManager", "Naloga created successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("NalogaManager", "Failed to create Naloga: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("NalogaManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
    }

    public void updateNaloga(int id, Naloga naloga, final Callback<Void> callback) {
        Call<Void> call = apiService.updateNaloga(id, naloga);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("NalogaManager", "Naloga updated successfully");
                    callback.onResponse(call, response);
                } else {
                    Log.e("NalogaManager", "Failed to update Naloga: " + response.message());
                    callback.onFailure(call, new Throwable(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("NalogaManager", "Error: " + t.getMessage());
                callback.onFailure(call, t);
            }
        });
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

    // SifrantByIdCallback.java
    public interface SifrantByIdCallback {
        void onSuccess(Sifrant sifrant);

        void onFailure(String errorMessage);
    }

    public interface RezervniDeliCallback {
        void onSuccess(List<RezervniDel> rezervniDeliList);

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
