package tech.bingulhan.webserver.response.impl.media;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import tech.bingulhan.webserver.app.MediaStructure;
import tech.bingulhan.webserver.response.NettyResponseHandler;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;

public class NettyResponseMediaHandler implements NettyResponseHandler {


    @Override
    public void handleResponse(NettyResponseService service, RequestStructure structure) {

        String path = structure.getRoot();
        String fileName = getFileNameFromPath(path);
        assert fileName != null;
        String fileExtension = getFileExtension(fileName);

        assert fileExtension != null;
        Optional<ResponseMediaType> mediaType = Arrays.stream(ResponseMediaType.values())
                .filter(mt -> mt.getExtension().equals(fileExtension))
                .findAny();

        if (!mediaType.isPresent()) {
            sendNotFound(service.getCtx());
            return;
        }

        Optional<MediaStructure> mediaStructure = service.getApplication().getData().getMediaStructures()
                .stream().filter(media -> media.getName().equals(fileName)).findAny();

        ChannelHandlerContext ctx = service.getCtx();

        if (mediaStructure.isPresent()) {
            File file = new File(mediaStructure.get().getPath());
            if (file.exists()) {
                try {
                    byte[] fileBytes = Files.readAllBytes(file.toPath());

                    FullHttpResponse response = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                            Unpooled.wrappedBuffer(fileBytes)
                    );

                    String contentType = getContentType(fileExtension);
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileBytes.length);
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                } catch (IOException e) {
                    e.printStackTrace();
                    sendInternalServerError(ctx);
                }
            } else {
                sendNotFound(ctx);
            }
        } else {
            sendNotFound(ctx);
        }
    }


    private String getContentType(String fileExtension) {
        switch (fileExtension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "mp4":
                return "video/mp4";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }

    private void sendNotFound(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
                Unpooled.copiedBuffer("File not found".getBytes())
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendInternalServerError(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.copiedBuffer("Internal Server Error!".getBytes())
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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
