package tech.bingulhan.webserver.response.impl;

import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.MediaStructure;
import tech.bingulhan.webserver.response.RequestStructure;
import tech.bingulhan.webserver.response.ResponseHandler;
import tech.bingulhan.webserver.response.ResponseService;

import java.io.*;
import java.net.*;
import java.util.HashMap;

import java.io.*;
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
            String contentType = getContentType(fileExtension);

            if (contentType == null) {
                sendErrorResponse("Not supported.", service);


                return;
            }

            Optional<MediaStructure> mediaStructure = AureliusApplication.MEDIA_STRUCTURES.
                    stream().filter(media -> media.getName().equals(fileName)).findAny();

            if (mediaStructure.isPresent()) {
                sendMediaFileWithBufferedOutputStream(service, mediaStructure.get(), contentType);

            }else{
                sendErrorResponse("File not found", service);

            }
        }
    }


    private String getContentType(String extension) {
        switch (extension) {
            case "png":
                return "image/png";
            case "jpeg":
            case "jpg":
                return "image/jpeg";
            case "mp4":
                return "video/mp4";
            default:
                return null;
        }
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

            byte[] buffer = new byte[16 * 1024];

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
    }


    private static byte[] readFile(File file) throws IOException {

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getName());
        }
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return data;
    }





}
