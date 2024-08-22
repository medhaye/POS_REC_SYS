package com.smartmssa.pos_recsys.service;

import org.json.JSONArray;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class OrderProcessor {

    public List<List<String>> processOrders(JSONArray orders) {
        Instant start = Instant.now();

        List<List<String>> transactions = IntStream.range(0, orders.length())
                .parallel()
                .mapToObj(orders::getJSONObject)
                .map(order -> {
                    JSONArray productsList = order.getJSONArray("productsList");
                    return IntStream.range(0, productsList.length())
                            .mapToObj(productsList::getJSONObject)
                            .map(product -> product.getString("productName"))
                            .collect(Collectors.toList());
                })
                .collect(Collectors.toList());

        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        System.out.println("Execution time in milliseconds: " + timeElapsed.toMillis());

        return transactions;
    }
}