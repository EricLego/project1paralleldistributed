package com.example.countdown.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Application layer service that encapsulates the countdown business logic.
 */
public class CountdownService {

    /**
     * Generates an integer countdown from {@code start} to 1.
     *
     * @param start positive starting value requested by the client
     * @return list containing {@code start, start-1, ..., 1}
     * @throws IllegalArgumentException when {@code start} is not positive
     */
    public List<Integer> generateCountdown(int start) {
        if (start <= 0) {
            throw new IllegalArgumentException("Countdown requires a positive integer");
        }

        List<Integer> values = new ArrayList<>(start);
        for (int current = start; current >= 1; current--) {
            values.add(current);
        }
        return values;
    }
}
