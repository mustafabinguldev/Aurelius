package tech.bingulhan.webserver.response;

import lombok.Getter;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.server.HttpServer;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ResponseService {


    private PrintWriter printWriter;

    private BufferedReader reader;

    private HttpServer webServer;

    @Getter
    private boolean cancelled = false;

    private String socketAdress = null;

    private Socket socket;

    @Getter
    private AureliusApplication application;

    public ResponseService(AureliusApplication application, BufferedReader reader, PrintWriter printWriter, HttpServer webServer, String socketAdress, Socket socket) {
        this.reader = reader;
        this.printWriter = printWriter;
        this.webServer = webServer;
        this.socketAdress = socketAdress;
        this.socket = socket;
        this.application = application;
    }
    public void addHttpData(String htmlContext)  {

        try {
            byte[] data = htmlContext.getBytes(StandardCharsets.UTF_8);
            this.socket.getOutputStream().write(data);
            this.socket.getOutputStream().flush();
        }catch (Exception exception) {
            exception.printStackTrace();
        }

    }


    public ResponseService add(String txt) {
        this.printWriter.print(txt);
        this.printWriter.flush();
        return this;
    }

    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    public String getRequestAddress() {
        return this.socketAdress;
    }
    public HttpServer getWebServer() {
        return webServer;
    }


    public void down()  {
        try {
            if (this.printWriter != null) {
                this.printWriter.flush();
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (printWriter != null) {
                printWriter.close();
            }
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public PrintWriter getPrintWriter() {
        return this.printWriter;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isSocketClosed() {
        return socket.isClosed() || !socket.isConnected();
    }



}
