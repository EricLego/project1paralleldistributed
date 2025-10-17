package com.example.countdown.concurrent;

import com.example.countdown.common.CountdownRequestHandler;
import com.example.countdown.service.CountdownService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Connection-oriented concurrent server that handles each client on a dedicated thread.
 */
public class ConcurrentCountdownServer {

    private final int port;
    private final ExecutorService executor;

    public ConcurrentCountdownServer(int port, int maxThreads) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(maxThreads);
    }

    public void start() throws IOException {
        CountdownRequestHandler handler = new CountdownRequestHandler(new CountdownService());
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("[Concurrent] Listening on port %d...%n", port);
            while (true) {
                Socket client = serverSocket.accept();
                System.out.printf("[Concurrent] Connected: %s%n", client.getRemoteSocketAddress());
                executor.submit(() -> {
                    try {
                        handler.handle(client);
                        System.out.printf("[Concurrent] Completed: %s%n", client.getRemoteSocketAddress());
                    } catch (IOException e) {
                        System.err.printf("[Concurrent] Error servicing %s: %s%n", client.getRemoteSocketAddress(), e.getMessage());
                    }
                });
            }
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5001;
        int maxThreads = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        ConcurrentCountdownServer server = new ConcurrentCountdownServer(port, maxThreads);
        try {
            server.start();
        } finally {
            server.shutdown();
        }
    }
}
