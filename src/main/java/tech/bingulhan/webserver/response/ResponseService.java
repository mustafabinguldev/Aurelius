package tech.bingulhan.webserver.response;

import lombok.Getter;
import tech.bingulhan.webserver.server.HttpServer;

import java.io.PrintWriter;

public class ResponseService {


    private PrintWriter printWriter;
    private HttpServer webServer;

    @Getter
    private boolean cancelled = false;

    private String socketAdress = null;
    public ResponseService(PrintWriter printWriter, HttpServer webServer, String socketAdress) {
        this.printWriter = printWriter;
        this.webServer = webServer;
        this.socketAdress = socketAdress;
    }
    public ResponseService addHttpData(String htmlContext) {
        this.printWriter.print(htmlContext+ "\r\n");
        return this;
    }

    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    public String getRequestAdress() {
        return this.socketAdress;
    }
    public HttpServer getWebServer() {
        return webServer;
    }
}
