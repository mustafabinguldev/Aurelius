package tech.bingulhan.webserver.response;

import tech.bingulhan.webserver.server.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author BingulHan
 */
public class WebSocketResponse {

    private final Socket socket;
    private final HttpServer webServer;
    public WebSocketResponse(HttpServer server, Socket socket){
        this.socket = socket;
        this.webServer = server;

        try {
            socket.setSoTimeout(5000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            handleRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleRequest() throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String requestLine = reader.readLine();
        RequestStructure requestStructure = requestData(requestLine);

        BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        PrintWriter out = new PrintWriter(this.socket.getOutputStream());


        ResponseService responseManager =
                new ResponseService(webServer.getApplication(), in,out,webServer,socket.getRemoteSocketAddress().toString(), socket);

        ResponseHandler.handle(responseManager, requestStructure);

    }

    private RequestStructure requestData(String line) {
        if (line != null) {
            String[] requestParts = line.split(" ");
            String method = requestParts[0];
            String url = requestParts[1];
            String path = url.split("\\?")[0];

            RequestStructure result = new RequestStructure();
            result.setMethod(method);
            result.setUrl(url);
            result.setRoot(path);
            result.setValid(true);

            return result;
        }

        return new RequestStructure();
    }
}
