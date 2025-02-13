package tech.bingulhan.webserver.server;

import tech.bingulhan.webserver.response.WebSocketResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author BingulHan
 */
public class HttpServer {

    private ServerSocket serverSocket;
    private final int port;
    private final int threadSize;
    private ExecutorService webServerService;

    public HttpServer(int port, int threadSize) {
        this.port = port;
        this.threadSize = threadSize;
    }

    public final void start() {
        this.webServerService = Executors.newCachedThreadPool();
        try {
            init();
            socketsHandler();
        } catch (IOException e) {
            e.printStackTrace();
            shutdown();
        }
    }


    private void init() throws IOException {
        serverSocket = new ServerSocket(this.port);
        System.out.println("Server started on port: " + this.port);
    }


    private void socketsHandler() {
        if (this.serverSocket == null || this.serverSocket.isClosed()) {
            throw new RuntimeException("Server socket is null or closed");
        }

        while (!serverSocket.isClosed()) {
            try {
                Socket socket = this.serverSocket.accept();

                this.webServerService.execute(() -> new WebSocketResponse(this, socket));

            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    public void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            shutdownExecutorService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shutdownExecutorService() {
        try {
            webServerService.shutdown();
            if (!webServerService.awaitTermination(60, TimeUnit.SECONDS)) {
                webServerService.shutdownNow();
            }
        } catch (InterruptedException e) {
            webServerService.shutdownNow();
        }
    }

    public boolean isServerSocketClosed() {
        return serverSocket.isClosed();
    }

}
