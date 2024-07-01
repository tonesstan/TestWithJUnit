package org.example;

import com.google.gson.Gson;
import models.AuthResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AdminRoutesTest {
    static String DB_URL = "jdbc:postgresql://localhost:5432/movies";
    static String DB_USER = "postgres";
    static String DB_PASSWORD = "postgres";
    static Connection connection;
    OkHttpClient client;

    @BeforeAll static void initDBConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            fail("Exception :" + e.getMessage());
        }
    }

    @AfterAll static void closeDBConnection() {
        try {
            AdminRoutesTest.connection.close();
        } catch (Exception e) {fail("Exception :" + e.getMessage());}
    }

    @BeforeEach void init() {
        this.client = new OkHttpClient.Builder().addInterceptor(chain-> {
            Request request = chain.request().newBuilder().addHeader("Authorization", "Bearer " + getAccessToken()).build();
            return chain.proceed(request);
        })
                .build();
    }

    static String getAccessToken() {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\"email\":\"admin@example.com\",\"password\":\"secret\"}", mediaType);
        Request request = new Request.Builder().url("http://localhost:4000/v1/authenticate").post(body).build();

        try {
            String response = Objects.requireNonNull(client.newCall(request).execute().body()).string();
            AuthResponse authResponse = new Gson().fromJson(response, AuthResponse.class);
            return authResponse.accessToken;
        } catch (Exception e) {
            fail("Exception :" + e.getMessage());
            return "";
        }
    }

    @Test void adminCanGetMovies() {
        Request request = new Request.Builder().url("http://localhost:4000/v1/movies").build();

        try {
            String response = Objects.requireNonNull(client.newCall(request).execute().body()).string();
            System.out.println(response);
            assertTrue(response.contains("title"));
        } catch (Exception e) {fail("Exception :" + e.getMessage());}
    }

    @Test void patchMovie() {
        String filmTitle = "";
        String filmID = "";
        try {
            Statement statement = AdminRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT title, id FROM movies LIMIT 1");
            while (resultSet.next()) {
                filmID = resultSet.getString("id");
                filmTitle = resultSet.getString("title");
            }
            System.out.println("Film title: " + filmTitle);
            System.out.println("Film ID: " + filmID);
        } catch (Exception e) {fail("Exception:" + e.getMessage());}

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\"title\":\"" + filmTitle + "\", \"description\":\"new description\"}", mediaType);
        Request request = new Request.Builder().url("http://localhost:4000/v1/movies"+filmID).patch(body).build();

        try {
            String response = this.client.newCall(request).execute().body().string();
            System.out.println(response);
            assertTrue(response.contains("new description"));
        } catch (Exception e) {fail("Exception :" + e.getMessage());}
    }

}