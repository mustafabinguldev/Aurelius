package tech.bingulhan.webserver.response.impl.media;

import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.MediaStructure;
import tech.bingulhan.webserver.response.RequestStructure;
import tech.bingulhan.webserver.response.ResponseHandler;
import tech.bingulhan.webserver.response.ResponseService;

import java.io.*;
import java.net.*;

import java.util.Arrays;
import java.util.Optional;

public class GetResponseMediaHandler implements ResponseHandler {

    @Override
    public void handleResponse(ResponseService service, RequestStructure structure) {

        String path = structure.getRoot();
        String fileName = getFileNameFromPath(path);
        assert fileName != null;
        String fileExtension = getFileExtension(fileName);

        if (structure.getMethod().equals("GET")) {
            if (structure.getRoot().equals("/favicon.ico")) {
                service.add("HTTP/1.1 400\r\n");
                service.add("Content-Type: text/html; charset=UTF-8\r\n");
                service.add("\r\n");
            }

            assert fileExtension != null;
            Optional<ResponseMediaType> mediaType = Arrays.stream(ResponseMediaType.values()).filter(mt->mt.getExtension().equals(fileExtension)).findAny();

            if (!mediaType.isPresent()) {
                sendErrorResponse("Not supported.", service);
                return;
            }

            Optional<MediaStructure> mediaStructure = service.getApplication().getData().getMediaStructures().
                    stream().filter(media -> media.getName().equals(fileName)).findAny();

            if (mediaStructure.isPresent()) {
                sendMediaFileWithBufferedOutputStream(service, mediaStructure.get(), mediaType.get().getContentType());

            }else{
                sendErrorResponse("File not found", service);
            }
        }
    }




    private void sendMediaFileWithBufferedOutputStream(ResponseService service, MediaStructure structure, String contentType) {
        File mediaFile = new File(structure.getPath());
        if (!mediaFile.exists()) {
            return;
        }

        StringBuilder responseHeaders = new StringBuilder();
        responseHeaders.append("HTTP/1.1 200 OK\r\n");
        responseHeaders.append("Content-Type: " + contentType + "\r\n");
        responseHeaders.append("Content-Length: " + mediaFile.length() + "\r\n");
        responseHeaders.append("Connection: close\r\n");
        responseHeaders.append("\r\n");

        try (BufferedOutputStream out = new BufferedOutputStream(service.getSocket().getOutputStream())) {
            out.write(responseHeaders.toString().getBytes());

            byte[] buffer = new byte[8 * 1024];

            try (FileInputStream fileInputStream = new FileInputStream(mediaFile)) {
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {

                    if (service.getSocket().isClosed() || !service.getSocket().isConnected()) {
                        break;
                    }
                    try {
                        out.write(buffer, 0, bytesRead);
                    }catch (SocketException exception) {
                        break;
                    }
                }
                out.flush();
                service.down();
            } catch (IOException ex) {
                ex.printStackTrace();
                service.add("HTTP/1.1 500 Internal Server Error\r\n");
                service.add("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            service.add("HTTP/1.1 500 Internal Server Error\r\n");
            service.add("\r\n");
        }
    }




    private void sendErrorResponse(String errorMessage, ResponseService service) {
        service.add("HTTP/1.1 500 Internal Server Error\r\n");
        service.add("Content-Type: text/html; charset=UTF-8\r\n");
        service.add("\r\n");
        service.add("<html><body><h1>" + errorMessage + "</h1></body></html>");
        service.down();

    }

    private String getFileNameFromPath(String path) {
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return null;
        }
        return path.substring(lastSlashIndex + 1);
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return null;
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

}
