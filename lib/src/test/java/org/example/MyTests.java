package org.example;

import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class MyTests {

    @Test void authenticate() {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create("{\"email\": \"admin@example.com\", \"password\": \"secret\"}", MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url("http://localhost:4000/v1/authenticate").post(body).build();
        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
        } catch (Exception e) {fail("Exception: " + e.getMessage());}
    }

    @Test void logOut() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://localhost:4000/v1/logout").build();
        try (Response response = client.newCall(request).execute()) {
            assertEquals(202, response.code());
        } catch (Exception e) {fail("Exception: " + e.getMessage());}
    }

    @Test void adminGetMovieById() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://localhost:4000/v1/admin/movies/1").header("Authorization", "Bearer " + AdminRoutesTest.getAccessToken()).build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assert response.body() != null;
            String body = response.body().string();
            assertTrue(body.contains("\"title\":\"Highlander\""));
        } catch (Exception e) {fail("Exception: " + e.getMessage());}
    }

    @Test void adminPutAndDeleteFilm() {
        OkHttpClient client = new OkHttpClient();
        String requestBody = """
                {
                  "description": "Very tough description for a very tough film",
                  "genres_array": [
                    1
                  ],
                  "id": 0,
                  "image": "No image",
                  "mpaa_rating": "PG13",
                  "release_date": "2021-06-20T00:00:00Z",
                  "runtime": 777,
                  "title": "The Film"
                }""";
        Request request = new Request.Builder().url("http://localhost:4000/v1/admin/movies/0").header("Authorization", "Bearer " + AdminRoutesTest.getAccessToken()).put(RequestBody.create(requestBody, MediaType.parse("application/json; charset=utf-8"))).build();

        String number = null;
        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assert response.body() != null;
            String responseBody = response.body().string();
            Pattern pattern = Pattern.compile("id (\\d+)");
            Matcher matcher = pattern.matcher(responseBody);
            if (matcher.find()) {
                number = matcher.group(1);
                System.out.println(number);
            }
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }

        request = new Request.Builder().url("http://localhost:4000/v1/admin/movies/" + number).header("Authorization", "Bearer " + AdminRoutesTest.getAccessToken()).delete().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
        } catch (Exception e) {fail("Exception: " + e.getMessage());}
    }
}


