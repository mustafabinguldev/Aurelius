package tech.bingulhan.webserver.response.impl.restful.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import tech.bingulhan.webserver.app.RestFulResponseStructure;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;
import tech.bingulhan.webserver.response.impl.restful.RestFulResponseHandler;

import java.nio.charset.StandardCharsets;


public class PostRestFulResponseHandler implements RestFulResponseHandler {



    @Override
    public void handle(NettyResponseService service, RequestStructure structure, RestFulResponseStructure responseStructure) {

        FullHttpRequest request = service.getRequest();
        String bodyJson = request.content().toString(StandardCharsets.UTF_8);
        Object o = null;

        try {
            o = responseStructure.getRestFulResponse().convert(bodyJson);
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.copiedBuffer(convertToJson(responseStructure.getRestFulResponse().response(o)), CharsetUtil.UTF_8)
            );

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
