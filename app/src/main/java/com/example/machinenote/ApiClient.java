package com.example.machinenote;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.12.192/apiv2/";
    private static Retrofit retrofit;
    private static String apiKey;

    public static void setApiKey(String apiKey) {
        ApiClient.apiKey = apiKey;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        Request request = chain.request();
                        if (apiKey != null) {
                            request = request.newBuilder()
                                    .addHeader("Authorization", apiKey)
                                    .build();
                        }
                        Response response = chain.proceed(request);
                        String rawJson = response.body().string();

                        // Log the raw JSON response
                        System.out.println("Raw JSON response: " + rawJson);

                        // Re-create the response before returning it because the rawJson string has already been consumed
                        return response.newBuilder()
                                .body(ResponseBody.create(response.body().contentType(), rawJson))
                                .build();
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getClient(int milliseconds) {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(milliseconds, TimeUnit.MILLISECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        Request request = chain.request();
                        if (apiKey != null) {
                            request = request.newBuilder()
                                    .addHeader("Authorization", apiKey)
                                    .build();
                        }
                        Response response = chain.proceed(request);
                        String rawJson = response.body().string();

                        // Log the raw JSON response
                        System.out.println("Raw JSON response: " + rawJson);

                        // Re-create the response before returning it because the rawJson string has already been consumed
                        return response.newBuilder()
                                .body(ResponseBody.create(response.body().contentType(), rawJson))
                                .build();
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
