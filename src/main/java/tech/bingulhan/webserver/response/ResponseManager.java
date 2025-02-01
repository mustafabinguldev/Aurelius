package tech.bingulhan.webserver.response;

import lombok.Getter;
import tech.bingulhan.webserver.server.WebServer;

import java.io.PrintWriter;

public class ResponseManager {


    private PrintWriter printWriter;
    private WebServer webServer;

    @Getter
    private boolean cancelled = false;

    private String socketAdress = null;
    public ResponseManager(PrintWriter printWriter, WebServer webServer, String socketAdress) {
        this.printWriter = printWriter;
        this.webServer = webServer;
        this.socketAdress = socketAdress;
    }
    public ResponseManager addHttpData(String htmlContext) {
        this.printWriter.print(htmlContext+ "\r\n");
        return this;
    }

    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    public String getRequestAdress() {
        return this.socketAdress;
    }
    public WebServer getWebServer() {
        return webServer;
    }
}
