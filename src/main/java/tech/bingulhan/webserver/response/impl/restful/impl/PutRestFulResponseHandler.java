package tech.bingulhan.webserver.response.impl.restful.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import tech.bingulhan.webserver.app.restful.RestFulResponseHelper;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;
import tech.bingulhan.webserver.response.impl.restful.RestFulResponseHandler;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;


public class PutRestFulResponseHandler implements RestFulResponseHandler {



    @Override
    public void handle(NettyResponseService service, RequestStructure structure, RestFulResponseStructure responseStructure) {

        FullHttpRequest request = service.getRequest();
        String bodyJson = request.content().toString(StandardCharsets.UTF_8);
        Object o = null;

        String cookieHeader = request.headers().get(HttpHeaders.Names.COOKIE);
        HashMap<String, String> receivedCookies = new HashMap<>();
        if (cookieHeader!=null) {
            Iterable<Cookie> cookies = CookieDecoder.decode(cookieHeader);
            cookies.forEach(cookie -> {
                receivedCookies.put(cookie.getName(), cookie.getValue());
            });
        }
        try {
            o = responseStructure.getRestFulResponse().convert(bodyJson);
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK
            );

            ByteBuf buf = Unpooled.copiedBuffer(convertToJson(
                    responseStructure.getRestFulResponse().response(o, new RestFulResponseHelper(service.getCtx(), structure, responseStructure,
                            response, receivedCookies))), CharsetUtil.UTF_8);
            response.content().clear().writeBytes(buf);

            responseStructure.getCookies().forEach(cookie-> {
                StringBuilder builder = new StringBuilder();
                builder.append(cookie.getCookieName() + "=" + cookie.getCookieValue() + ";");
                cookie.getFeatures().forEach(cookieFeature -> builder.append(" " + cookieFeature.toValue() + ";"));
                response.headers().add(HttpHeaderNames.SET_COOKIE, builder.toString());
            });


            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            service.getCtx().writeAndFlush(response);
        } catch (Exception e) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
                    Unpooled.copiedBuffer("", CharsetUtil.UTF_8)
            );

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            service.getCtx().writeAndFlush(response);
        }


    }


    private String convertToJson(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error converting to JSON";
        }
    }
}
