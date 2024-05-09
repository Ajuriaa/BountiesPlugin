package org.wasalona.bounties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrencyParser {

    public static List<String[]> parseCurrencyString(String currencyString) {
        List<String[]> resultList = new ArrayList<>();

        String[] currencyPairs = currencyString.split(", ");
        for (String pair : currencyPairs) {
            String[] parts = pair.split(": ");
            String name = parts[0].split("_")[2];
            int amount = Integer.parseInt(parts[1]);
            resultList.add(new String[] { name.toLowerCase(), String.valueOf(amount) });
        }

        return resultList;
    }

    public static Map<String, Integer> convertToMap(String input) {
        Map<String, Integer> resultMap = new HashMap<>();

        String[] keyValuePairs = input.split(",");

        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split(": ");
            String key = keyValue[0].split("_")[2]; // Extract the currency type (e.g., IRON, EMERALD)
            int value = Integer.parseInt(keyValue[1]);
            resultMap.put(key, value);
        }

        return resultMap;
    }
}

