package com.example.machinenote;

import android.content.Context;
import android.util.Log;

import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.Role;
import com.example.machinenote.models.Zastoj;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiManager {
    private ApiService apiService;
    private SharedPreferencesHelper sharedPreferencesHelper;

    public ApiManager(Context context) {
        apiService = ApiClient.getClient().create(ApiService.class);
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
    }

    public void login(String username, String password, LoginCallback callback) {

        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<LoginResponse> call = apiService.login(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d("json", response.message());
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

}
