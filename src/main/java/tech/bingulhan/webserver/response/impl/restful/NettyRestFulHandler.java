package tech.bingulhan.webserver.response.impl.restful;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.response.NettyResponseHandler;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NettyRestFulHandler implements NettyResponseHandler {

    @Override
    public void handleResponse(NettyResponseService service, RequestStructure structure) {

        Optional<RestFulResponseStructure> optionalRestFulResponseStructure = handle(structure);

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

    private Optional<RestFulResponseStructure> handle(RequestStructure structure) {
        List<RestFulResponseStructure> restfulResponseStructures = AureliusApplication.getInstance().getRestFulResponseStructures();

        String root = structure.getRoot();
        String[] params = root.split("/");


        if (params.length < 3) return Optional.empty();
        String endpoint = params[2];
        String[] pathDatas = Arrays.copyOfRange(params, 3, params.length);
        Optional<RestFulResponseStructure> exactMatch = restfulResponseStructures
                .stream()
                .filter(str -> ("/api/" + str.getRoot()).equals(root))
                .findAny();

        if (exactMatch.isPresent()) {
            structure.setPathDatas(new String[0]);
            return exactMatch;
        }

        Optional<RestFulResponseStructure> dynamicMatch = restfulResponseStructures
                .stream()
                .filter(str -> root.startsWith("/api/" + str.getRoot()))
                .max(Comparator.comparingInt(str -> str.getRoot().length()));

        if (dynamicMatch.isPresent()) {
            structure.setPathDatas(pathDatas);
            return dynamicMatch;
        }

        return Optional.empty();
    }

}
