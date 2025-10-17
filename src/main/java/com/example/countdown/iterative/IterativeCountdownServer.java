package com.example.countdown.iterative;

import com.example.countdown.common.CountdownRequestHandler;
import com.example.countdown.service.CountdownService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Connection-oriented iterative server that processes one client at a time.
 */
public class IterativeCountdownServer {

    private final int port;

    public IterativeCountdownServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        CountdownRequestHandler handler = new CountdownRequestHandler(new CountdownService());
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("[Iterative] Listening on port %d...%n", port);
            while (true) {
                Socket client = serverSocket.accept();
                System.out.printf("[Iterative] Connected: %s%n", client.getRemoteSocketAddress());
                handler.handle(client);
                System.out.printf("[Iterative] Completed: %s%n", client.getRemoteSocketAddress());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        new IterativeCountdownServer(port).start();
    }
}
