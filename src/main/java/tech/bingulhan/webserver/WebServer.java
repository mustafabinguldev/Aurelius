package tech.bingulhan.webserver;

import lombok.Getter;
import tech.bingulhan.webserver.listeners.HttpListener;
import tech.bingulhan.webserver.response.SocketResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


/**
 * @author BingulHan
 */
public class WebServer {


    protected ServerSocket serverSocket;

    private final int port;
    private final int threadSize;

    @Getter
    private Logger logger = Logger.getLogger("WebServer");

    private ExecutorService webServerService;

    @Getter
    private List<HttpListener> httpListeners;

    public WebServer(int port, int threedSize) {
        this.port = port;
        this.threadSize = threedSize;

        httpListeners = new LinkedList<>();
    }

    public final void start(){
        getLogger().info("WebServer v0.1 has starting...");
        getLogger().info("Author: BingulHan");

        this.webServerService = Executors.newFixedThreadPool(this.threadSize);
        try {
            init();
            socketsHandler();
        }catch (Exception exception) {
            logger.warning(exception.getMessage());
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

            WebServer finalWebServer = this;
            this.webServerService.execute(new Runnable() {
                @Override
                public void run() {
                    new SocketResponse(finalWebServer,socket);
                }
            });
        }
    }

    public WebServer addListener(HttpListener httpListener) {
        httpListeners.add(httpListener);
        return this;
    }



}
