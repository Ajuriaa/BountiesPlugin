package org.wasalona.bounties;

import java.util.ArrayList;
import java.util.List;

public class CurrencyParser {

    public static List<String[]> parseCurrencyString(String currencyString) {
        List<String[]> resultList = new ArrayList<>();

        String[] currencyPairs = currencyString.split(", ");
        for (String pair : currencyPairs) {
            String[] parts = pair.split(": ");
            String name = parts[0].split("_")[3];
            int amount = Integer.parseInt(parts[1]);
            resultList.add(new String[] { name.toLowerCase(), String.valueOf(amount) });
        }

        return resultList;
    }
}

