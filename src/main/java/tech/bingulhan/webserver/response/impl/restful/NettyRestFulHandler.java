package tech.bingulhan.webserver.response.impl.restful;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.response.NettyResponseHandler;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.util.Optional;

public class NettyRestFulHandler implements NettyResponseHandler {

    @Override
    public void handleResponse(NettyResponseService service, RequestStructure structure) {

        Optional<RestFulResponseStructure> optionalRestFulResponseStructure = AureliusApplication.getInstance().
                getRestFulResponseStructures().
                stream().
                filter(str->
                    structure.getRoot().equals("/api/"+str.getRoot())).
                findAny();

        if (optionalRestFulResponseStructure.isPresent()) {

            if (!optionalRestFulResponseStructure.get().getRequestType().toString().equals(structure.getMethod())) {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
                        Unpooled.copiedBuffer("", CharsetUtil.UTF_8)
                );

                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                service.getCtx().writeAndFlush(response);
                return;
            }

            optionalRestFulResponseStructure.get().getRequestType().getHandler().handle(service, structure, optionalRestFulResponseStructure.get());
        }else{
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.copiedBuffer("404", CharsetUtil.UTF_8)
            );
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            service.getCtx().writeAndFlush(response);
        }
    }

}
