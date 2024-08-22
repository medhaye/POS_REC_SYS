package com.smartmssa.pos_recsys.service;

import org.json.JSONArray;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Component
public class OrderFetcher {
    public JSONArray getOrders(String urlString, String token) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        try (InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            return new JSONArray(responseBody);
        }
    }
}