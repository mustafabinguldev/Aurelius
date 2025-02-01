package tech.bingulhan.webserver.response;

import tech.bingulhan.webserver.server.WebServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author BingulHan
 */
public class SocketResponse {

    private Socket socket;
    private WebServer webServer;
    public SocketResponse(WebServer server,Socket socket){
        this.socket = socket;
        this.webServer = server;

        try {
            sendData();
        } catch (IOException e) {
            server.getLogger().warning(e.getMessage());
        }
    }

    public void sendData() throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String requestLine = reader.readLine();

        String root = "";

        if (requestLine != null) {
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String url = requestParts[1];


            String path = url.split("\\?")[0];
            String queryParams = url.contains("?") ? url.split("\\?")[1] : "";
            root = path;


            if (!queryParams.isEmpty()) {
                String[] params = queryParams.split("&");
                for (String param : params) {
                    String[] paramPair = param.split("=");
                    String key = paramPair[0];
                    String value = paramPair.length > 1 ? paramPair[1] : null;

                }
            }

        }


        BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        PrintWriter out = new PrintWriter(this.socket.getOutputStream());

        out.print("HTTP/1.1 200 OK\r\n"); // Version & status code
        out.print("Content-Type: text/html; charset=UTF-8\r\n"); // The type of data
        out.print("\r\n"); // End of headers


        ResponseManager responseManager = new ResponseManager(out,webServer,socket.getRemoteSocketAddress().toString());
        String finalRoot = root;
        webServer.getHttpListeners().forEach(l->l.onHttpRequest(responseManager, finalRoot));


        if (!responseManager.isCancelled()) {
            out.close();
        }

        in.close(); // Close the input stream
        this.socket.close(); // Close the socket itself


    }
}
