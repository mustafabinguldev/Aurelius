package tech.bingulhan.webserver.server;

import lombok.Getter;
import tech.bingulhan.webserver.response.WebSocketResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


/**
 * @author BingulHan
 */
public class HttpServer {


    protected ServerSocket serverSocket;

    private final int port;
    private final int threadSize;

    @Getter
    private Logger logger = Logger.getLogger("WebServer");

    private ExecutorService webServerService;

    public HttpServer(int port, int threedSize) {
        this.port = port;
        this.threadSize = threedSize;
    }

    public final void start(){
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

            HttpServer finalWebServer = this;


            this.webServerService.execute(new Runnable() {
                @Override
                public void run() {
                    new WebSocketResponse(finalWebServer,socket);
                }
            });
        }
    }





}
