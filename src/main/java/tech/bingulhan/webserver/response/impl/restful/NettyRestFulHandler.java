package tech.bingulhan.webserver.response.impl.restful;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.response.NettyResponseHandler;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NettyRestFulHandler implements NettyResponseHandler {
    @Override
    public void handleResponse(NettyResponseService service, RequestStructure structure) {
        String path = structure.getRoot();
        List<RestFulResponseStructure> routes = service.getApplication().getRestFulResponseStructures();
        Optional<String> matchedRoot = routes.stream()
                .map(RestFulResponseStructure::getRoot)
                .filter(root -> path.equals("/api/" + root) || path.startsWith("/api/" + root + "/"))
                .max(Comparator.comparingInt(String::length));

        if (!matchedRoot.isPresent()) {
            sendError(service, HttpResponseStatus.NOT_FOUND, "404", null);
            return;
        }

        // Resolve the most specific path before the method: do not fall back
        // to a parent endpoint when a child does not support the method.
        List<RestFulResponseStructure> matchingRoutes = routes.stream()
                .filter(route -> matchedRoot.get().equals(route.getRoot()))
                .collect(Collectors.toList());
        Optional<RestFulResponseStructure> match = matchingRoutes.stream()
                .filter(route -> route.getRequestType().name().equals(structure.getMethod()))
                .findFirst();

        if (!match.isPresent()) {
            String allow = matchingRoutes.stream().map(route -> route.getRequestType().name())
                    .distinct().sorted().collect(Collectors.joining(", "));
            sendError(service, HttpResponseStatus.METHOD_NOT_ALLOWED, "", allow);
            return;
        }

        String suffix = path.substring(("/api/" + matchedRoot.get()).length());
        structure.setPathDatas(suffix.isEmpty() || suffix.equals("/")
                ? new String[0] : suffix.substring(1).split("/"));
        RestFulResponseStructure route = match.get();
        route.getRequestType().getHandler().handle(service, structure, route);
    }

    private void sendError(NettyResponseService service, HttpResponseStatus status, String body, String allow) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(body, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (allow != null) response.headers().set(HttpHeaderNames.ALLOW, allow);
        service.getCtx().writeAndFlush(response);
    }
}
