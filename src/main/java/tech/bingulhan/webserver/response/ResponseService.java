package tech.bingulhan.webserver.response;

import lombok.Getter;
import tech.bingulhan.webserver.server.HttpServer;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ResponseService {


    private PrintWriter printWriter;

    private BufferedReader reader;

    private HttpServer webServer;

    @Getter
    private boolean cancelled = false;

    private String socketAdress = null;

    private Socket socket;

    public ResponseService(BufferedReader reader,PrintWriter printWriter, HttpServer webServer, String socketAdress, Socket socket) {
        this.reader = reader;
        this.printWriter = printWriter;
        this.webServer = webServer;
        this.socketAdress = socketAdress;
        this.socket = socket;
    }
    public ResponseService addHttpData(String htmlContext) {
        this.printWriter.print(htmlContext+ "\r\n");
        return this;
    }

    public ResponseService add(String txt) {
        this.printWriter.print(txt);
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

    public void writeDataToOutputStream(byte[] data, int offset, int length) throws IOException {
        socket.getOutputStream().write(data, offset, length);
        socket.getOutputStream().flush();
    }


    public void down() throws IOException {

        reader.close();
        printWriter.close();
        socket.close();

    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isSocketClosed() {
        return socket.isClosed() || !socket.isConnected();
    }



}
