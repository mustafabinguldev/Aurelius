package tech.bingulhan.webserver.response;

import tech.bingulhan.webserver.server.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author BingulHan
 */
public class WebSocketResponse {

    private Socket socket;
    private HttpServer webServer;
    public WebSocketResponse(HttpServer server, Socket socket){
        this.socket = socket;
        this.webServer = server;

        try {
            handleRequest();
        } catch (IOException e) {
            //
        }
    }

    public void handleRequest() throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String requestLine = reader.readLine();
        RequestStructure requestStructure = requestData(requestLine);

        BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        PrintWriter out = new PrintWriter(this.socket.getOutputStream());


        ResponseService responseManager =
                new ResponseService(in,out,webServer,socket.getRemoteSocketAddress().toString(), socket);

        ResponseHandler.handle(responseManager, requestStructure);

        out.close();
        in.close();
        socket.close();
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
