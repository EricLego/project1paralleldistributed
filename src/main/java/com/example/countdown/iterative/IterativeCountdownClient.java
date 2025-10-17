package com.example.countdown.iterative;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client for the iterative server variant.
 */
public class IterativeCountdownClient {

    private final String host;
    private final int port;

    public IterativeCountdownClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void requestCountdown(int value) throws IOException {
        try (Socket socket = new Socket(host, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.println(value);

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: IterativeCountdownClient <value> [host] [port]");
            return;
        }
        int value = Integer.parseInt(args[0]);
        String host = args.length > 1 ? args[1] : "localhost";
        int port = args.length > 2 ? Integer.parseInt(args[2]) : 5000;

        new IterativeCountdownClient(host, port).requestCountdown(value);
    }
}
