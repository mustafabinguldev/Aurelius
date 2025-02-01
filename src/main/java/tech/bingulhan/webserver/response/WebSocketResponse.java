package tech.bingulhan.webserver.response;

import tech.bingulhan.webserver.response.impl.GetResponseMvcHandler;
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
            server.getLogger().warning(e.getMessage());
        }
    }

    public void handleRequest() throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String requestLine = reader.readLine();
        System.out.println(requestLine);
        RequestStructure requestStructure = requestData(requestLine);

        BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        PrintWriter out = new PrintWriter(this.socket.getOutputStream());

        out.print("HTTP/1.1 200 OK\r\n");
        out.print("Content-Type: text/html; charset=UTF-8\r\n");
        out.print("\r\n");

        ResponseService responseManager = new ResponseService(out,webServer,socket.getRemoteSocketAddress().toString());

        new GetResponseMvcHandler().handleResponse(responseManager, requestStructure);
        if (!responseManager.isCancelled()) {
            out.close();
        }

        in.close();
        this.socket.close();


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
            /*
            String queryParams = url.contains("?") ? url.split("\\?")[1] : "";
            root = path;
            if (!queryParams.isEmpty()) {
                String[] params = queryParams.split("&");
                for (String param : params) {
                    String[] paramPair = param.split("=");
                    String key = paramPair[0];
                    String value = paramPair.length > 1 ? paramPair[1] : null;
                }
            }*/

        }

        return new RequestStructure();
    }
}
