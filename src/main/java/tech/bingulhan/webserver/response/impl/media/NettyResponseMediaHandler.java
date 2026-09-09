package tech.bingulhan.webserver.response.impl.media;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import tech.bingulhan.webserver.app.mvc.MediaStructure;
import tech.bingulhan.webserver.response.NettyResponseHandler;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public class NettyResponseMediaHandler implements NettyResponseHandler {
    @Override
    public void handleResponse(NettyResponseService service, RequestStructure structure) {
        HttpMethod method = service.getRequest().method();
        if (!HttpMethod.GET.equals(method) && !HttpMethod.HEAD.equals(method)) {
            sendError(service, HttpResponseStatus.METHOD_NOT_ALLOWED, "Method not allowed", "GET, HEAD");
            return;
        }
        String path = structure.getRoot();
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        int dot = fileName.lastIndexOf('.');
        String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        Optional<ResponseMediaType> type = Arrays.stream(ResponseMediaType.values())
                .filter(candidate -> candidate.getExtension().equals(extension)).findFirst();
        Optional<MediaStructure> media = service.getApplication().getData().getMediaStructures().stream()
                .filter(candidate -> candidate.getName().equals(fileName)).findFirst();
        if (!type.isPresent() || !media.isPresent()) {
            sendError(service, HttpResponseStatus.NOT_FOUND, "File not found", null);
            return;
        }
        Path file = Path.of(media.get().getPath());
        if (!Files.isRegularFile(file)) {
            sendError(service, HttpResponseStatus.NOT_FOUND, "File not found", null);
            return;
        }
        FileChannel input = null;
        try {
            input = FileChannel.open(file, StandardOpenOption.READ);
            long length = input.size();
            if (HttpMethod.HEAD.equals(method)) {
                input.close();
                input = null;
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                setHeaders(response, type.get().getContentType(), length);
                service.getCtx().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return;
            }
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            setHeaders(response, type.get().getContentType(), length);
            // Netty owns and closes the file channel when the region is released,
            // including failed writes and disconnected clients. No whole-file byte[].
            DefaultFileRegion region = new DefaultFileRegion(input, 0, length);
            input = null;
            service.getCtx().write(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            service.getCtx().write(region).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            service.getCtx().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
        } catch (IOException e) {
            sendError(service, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null);
        } finally {
            if (input != null) {
                try { input.close(); } catch (IOException ignored) { }
            }
        }
    }

    private static void setHeaders(HttpResponse response, String type, long length) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, type);
        HttpUtil.setContentLength(response, length);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
    }

    private static void sendError(NettyResponseService service, HttpResponseStatus status, String message, String allow) {
        boolean head = HttpMethod.HEAD.equals(service.getRequest().method());
        byte[] bytes = message.getBytes(CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                head ? Unpooled.EMPTY_BUFFER : Unpooled.wrappedBuffer(bytes));
        setHeaders(response, "text/plain; charset=UTF-8", bytes.length);
        if (allow != null) response.headers().set(HttpHeaderNames.ALLOW, allow);
        service.getCtx().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
