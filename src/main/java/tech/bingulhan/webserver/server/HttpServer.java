package tech.bingulhan.webserver.server;

import lombok.Getter;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.response.WebSocketResponse;

import javax.swing.*;
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

    private ExecutorService webServerService;

    @Getter
    private AureliusApplication application;

    String[] args;

    public HttpServer(AureliusApplication application, int port, String[] args) {
        this.port = port;
        this.application = application;
        this.args = args;
    }

    public final void start() {
        this.webServerService = Executors.newCachedThreadPool();
        try {
            init();
            socketsHandler();
        } catch (IOException e) {
            System.err.println("Aurelius cannot be opened with "+AureliusApplication.getInstance().getPort()+" port.");
            JOptionPane.showMessageDialog(null, "Aurelius cannot be opened with "+AureliusApplication.getInstance().getPort()+" port.", "Error", JOptionPane.ERROR_MESSAGE);
            shutdown();
        }
    }


    private void init() throws IOException {
        serverSocket = new ServerSocket(this.port);
        System.out.println("Server started on port: " + this.port);

        if (application.isUi()) {
            this.webServerService.execute(() -> application.getApplicationUI().load(args));
        }

    }


    private void socketsHandler() {
        if (this.serverSocket == null || this.serverSocket.isClosed()) {
            JOptionPane.showMessageDialog(null, "Server socket is null or closed", "Error", JOptionPane.ERROR_MESSAGE);
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
            System.exit(0);
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
