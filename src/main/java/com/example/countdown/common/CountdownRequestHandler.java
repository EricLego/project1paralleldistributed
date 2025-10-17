package com.example.countdown.common;

import com.example.countdown.service.CountdownService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * Infrastructure-layer helper that bridges socket I/O with the application service.
 */
public class CountdownRequestHandler {

    private final CountdownService service;

    public CountdownRequestHandler(CountdownService service) {
        this.service = service;
    }

    /**
     * Processes a single countdown request on the provided socket.
     * The protocol expects a single line containing an integer. The handler responds
     * with one integer per line following the countdown protocol before closing the socket.
     */
    public void handle(Socket clientSocket) throws IOException {
        try (Socket socket = clientSocket;
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             PrintWriter writer = new PrintWriter(out, true)) {

            String line = reader.readLine();
            if (line == null) {
                writer.println("ERROR: missing countdown value");
                return;
            }

            try {
                int start = Integer.parseInt(line.trim());
                List<Integer> values = service.generateCountdown(start);
                for (Integer value : values) {
                    writer.println(value);
                }
            } catch (IllegalArgumentException e) {
                writer.println("ERROR: " + e.getMessage());
            }
        }
    }
}
