package com.elegion.myfirstapplication;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.elegion.myfirstapplication.model.User;
import com.elegion.myfirstapplication.model.converter.DataConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by marat.taychinov
 */

public class ApiUtils {

    public static final List<Class<? extends java.io.IOException>> NETWORK_EXCEPTION = Arrays.asList(
            UnknownHostException.class,
            SocketTimeoutException.class,
            ConnectException.class
    );

    private static OkHttpClient client;
    private static Retrofit retrofit;
    private static Gson gson;
    private static AcademyApi api;

    public static OkHttpClient getBasicAuthClient(final String email, final String password, boolean createNewInstance) {
        if (createNewInstance || client == null) {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.authenticator(new Authenticator() {
                @Nullable
                @Override
                public Request authenticate(@NonNull Route route, @NonNull Response response) throws IOException {
                    String credential = Credentials.basic(email, password);
                    // If we already failed with these credentials, don't retry.
                    if (credential.equals(response.request().header("Authorization"))) {
                        return null;
                    }
                    return response.request().newBuilder().header("Authorization", credential).build();
                }
            });
            if (!BuildConfig.BUILD_TYPE.contains("release")) {
                builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }

            client = builder.build();
        }
        return client;
    }

    public static Retrofit getRetrofit() {
        if (gson == null) {
            gson = new Gson();
        }
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.SERVER_URL)
                    // need for interceptors
                    .client(getBasicAuthClient("", "", false))
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit rebuildRetrofit(OkHttpClient client) {
        if (gson == null) {
            gson = new Gson();
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                // need for interceptors
                .client(client)
                .addConverterFactory(new DataConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        return retrofit;
    }

    public static AcademyApi getApiService() {
        if (api == null) {
            api = getRetrofit().create(AcademyApi.class);
        }
        return api;
    }

    public static AcademyApi getApiService(String email, String password, boolean createNewInstance) {
        if (createNewInstance || api == null) {


            api = rebuildRetrofit(getBasicAuthClient(
                  email,
                  password,
                    true))
                    .create(AcademyApi.class);
        }
        return api;
    }

    public static ApiError parseError(retrofit2.Response<?> response, int statusCode) {
        Converter<ResponseBody, ApiError> converter =
                ApiUtils.getRetrofit()
                        .responseBodyConverter(ApiError.class, new Annotation[0]);

        ApiError error;

        try {
            error = converter.convert(response.errorBody());
            error.setCode(statusCode);
        } catch (IOException e) {
            error = new ApiError(statusCode);
        }

        return error;
    }

}
