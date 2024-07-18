package com.example.machinenote;

import android.content.Context;
import android.util.Log;

import com.example.machinenote.Utility.SharedPreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginManager {
    private ApiService apiService;
    private SharedPreferencesHelper sharedPreferencesHelper;

    public LoginManager(Context context) {
        apiService = ApiClient.getClient().create(ApiService.class);
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
    }

    public void login(String username, String password, LoginCallback callback) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<LoginResponse> call = apiService.login(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String apiKey = response.body().getApiKey();
                    sharedPreferencesHelper.putString(SharedPreferencesHelper.Token, apiKey);
                    sharedPreferencesHelper.putString(SharedPreferencesHelper.Username, username);
                    sharedPreferencesHelper.putString(SharedPreferencesHelper.Password, password);
                    Log.d("success", "Logged in with user " + username);
                    callback.onSuccess();
                } else {
                    Log.e("error", String.valueOf(response.body()));
                    callback.onFailure("Login failed");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("error", String.valueOf(t));
                callback.onFailure(t.getMessage());
            }
        });
    }

    public interface LoginCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
