package tech.bingulhan.webserver.response.impl.react;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import tech.bingulhan.webserver.response.NettyResponseHandler;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

/**
 * Serves a React single-page application build (for example Vite's or Create React App's output)
 * from the application's {@code react/} folder. Existing build files are returned as static files;
 * extensionless paths fall back to {@code index.html} so client-side routing works on reload.
 */
public class NettyResponseReactHandler implements NettyResponseHandler {

    private static final String INDEX = "index.html";

    @Override
    public void handleResponse(NettyResponseService service, RequestStructure structure) {
        HttpMethod method = service.getRequest().method();
        if (!HttpMethod.GET.equals(method) && !HttpMethod.HEAD.equals(method)) {
            sendError(service, HttpResponseStatus.METHOD_NOT_ALLOWED, "Method not allowed", "GET, HEAD");
            return;
        }
        File folder = service.getApplication().getData().getPathData().getReactFolder();
        Path root = folder.toPath().toAbsolutePath().normalize();

        String requestPath;
        try {
            requestPath = QueryStringDecoder.decodeComponent(structure.getRoot());
        } catch (IllegalArgumentException e) {
            sendError(service, HttpResponseStatus.BAD_REQUEST, "Bad request", null);
            return;
        }
        String relative = requestPath.replace('\\', '/').replaceAll("^/+", "");
        Path file = relative.isEmpty() ? root.resolve(INDEX) : root.resolve(relative).normalize();
        if (!file.startsWith(root) || relative.indexOf('\0') >= 0) {
            sendError(service, HttpResponseStatus.NOT_FOUND, "File not found", null);
            return;
        }
        if (Files.isDirectory(file)) {
            file = file.resolve(INDEX);
        }
        if (!Files.isRegularFile(file)) {
            // Paths that look like files (e.g. /assets/missing.js) must not receive HTML.
            String name = relative.substring(relative.lastIndexOf('/') + 1);
            if (name.contains(".")) {
                sendError(service, HttpResponseStatus.NOT_FOUND, "File not found", null);
                return;
            }
            file = root.resolve(INDEX);
            if (!Files.isRegularFile(file)) {
                sendError(service, HttpResponseStatus.NOT_FOUND, "File not found", null);
                return;
            }
        }
        sendFile(service, method, file, root);
    }

    private static void sendFile(NettyResponseService service, HttpMethod method, Path file, Path root) {
        String contentType = ReactContentType.of(file.getFileName().toString());
        String cacheControl = cacheControl(root.relativize(file));
        FileChannel input = null;
        try {
            input = FileChannel.open(file, StandardOpenOption.READ);
            long length = input.size();
            if (HttpMethod.HEAD.equals(method)) {
                input.close();
                input = null;
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                setHeaders(response, contentType, length);
                response.headers().set(HttpHeaderNames.CACHE_CONTROL, cacheControl);
                service.getCtx().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return;
            }
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            setHeaders(response, contentType, length);
            response.headers().set(HttpHeaderNames.CACHE_CONTROL, cacheControl);
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

    /**
     * Bundlers put content-hashed files under {@code assets/} (Vite) or {@code static/} (Create React App),
     * so those can be cached forever. Everything else, especially index.html, must be revalidated.
     */
    static String cacheControl(Path relative) {
        String first = relative.getNameCount() > 1 ? relative.getName(0).toString().toLowerCase(Locale.ROOT) : "";
        if (first.equals("assets") || first.equals("static")) {
            return "public, max-age=31536000, immutable";
        }
        return "no-cache";
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
