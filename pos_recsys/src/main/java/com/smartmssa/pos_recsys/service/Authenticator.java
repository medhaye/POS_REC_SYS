package com.smartmssa.pos_recsys.service;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Component
public class Authenticator {
    public String login(String urlString, String username, String password) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == 308) {
            String newUrl = conn.getHeaderField("Location");
            System.out.println("Redirected to URL: " + newUrl);
            return login(newUrl, username, password);
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }

        try (Scanner scanner = new Scanner(conn.getInputStream())) {
            String responseBody = scanner.useDelimiter("\\A").next();
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getString("token");
        }
    }
}