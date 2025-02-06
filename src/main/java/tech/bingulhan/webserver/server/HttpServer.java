package tech.bingulhan.webserver.server;


import tech.bingulhan.webserver.response.WebSocketResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author BingulHan
 */
public class HttpServer {


    protected ServerSocket serverSocket;

    private final int port;
    private final int threadSize;

    private ExecutorService webServerService;

    public HttpServer(int port, int threadSize) {
        this.port = port;
        this.threadSize = threadSize;
    }

    public final void start(){
        this.webServerService = Executors.newFixedThreadPool(this.threadSize);
        try {
            init();
            socketsHandler();
        }catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    private void init() throws IOException {
        serverSocket = new ServerSocket(this.port);
    }

    private void socketsHandler() throws RuntimeException, IOException{
        if (this.serverSocket==null || this.serverSocket.isClosed()) {
            throw new RuntimeException("Server socket is null or closed");
        }

        while(!serverSocket.isClosed()) {
            Socket socket = this.serverSocket.accept();
            HttpServer finalWebServer = this;
            this.webServerService.execute(() -> new WebSocketResponse(finalWebServer,socket));
        }
    }





}
