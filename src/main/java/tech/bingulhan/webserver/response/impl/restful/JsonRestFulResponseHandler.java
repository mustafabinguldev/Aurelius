package tech.bingulhan.webserver.response.impl.restful;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.CharsetUtil;
import tech.bingulhan.webserver.app.restful.RestFulResponseHelper;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.util.HashMap;

/** Shared, immutable mapper configuration and one owned response buffer. */
public class JsonRestFulResponseHandler implements RestFulResponseHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void handle(NettyResponseService service, RequestStructure structure, RestFulResponseStructure route) {
        Object body;
        HashMap<String, String> cookies = new HashMap<>();
        try {
            String header = service.getRequest().headers().get(HttpHeaderNames.COOKIE);
            if (header != null) ServerCookieDecoder.LAX.decode(header)
                    .forEach(cookie -> cookies.put(cookie.name(), cookie.value()));
            body = route.getRestFulResponse().convert(service.getRequest().content().toString(CharsetUtil.UTF_8));
        } catch (Exception e) {
            sendError(service, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                service.getCtx().alloc().buffer());
        try {
            Object result = route.getRestFulResponse().response(body,
                    new RestFulResponseHelper(service.getCtx(), structure, route, response, cookies));
            MAPPER.writeValue((java.io.OutputStream) new ByteBufOutputStream(response.content()), result);
            route.getCookies().forEach(cookie -> {
                StringBuilder value = new StringBuilder(cookie.getCookieName()).append('=')
                        .append(cookie.getCookieValue()).append(';');
                cookie.getFeatures().forEach(feature -> value.append(' ').append(feature.toValue()).append(';'));
                response.headers().add(HttpHeaderNames.SET_COOKIE, value.toString());
            });
        } catch (Exception e) {
            response.release();
            sendError(service, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        write(service, response);
    }

    private static void sendError(NettyResponseService service, HttpResponseStatus status) {
        write(service, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status));
    }

    private static void write(NettyResponseService service, FullHttpResponse response) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        boolean keepAlive = HttpUtil.isKeepAlive(service.getRequest());
        HttpUtil.setKeepAlive(response, keepAlive);
        ChannelFuture write = service.getCtx().writeAndFlush(response);
        write.addListener(keepAlive ? ChannelFutureListener.CLOSE_ON_FAILURE : ChannelFutureListener.CLOSE);
    }
}
