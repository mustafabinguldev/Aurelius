package tech.bingulhan.webserver.server.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.response.NettyResponseHandler;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

public class HttpRequestHandler extends SimpleChannelInboundHandler<DefaultHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultHttpRequest msg) throws Exception {

        if (msg.method() == HttpMethod.GET) {

            RequestStructure requestStructure = new RequestStructure();
            requestStructure.setValid(msg.uri() != null && !msg.uri().isEmpty());
            requestStructure.setMethod(msg.method().name());

            requestStructure.setUrl(msg.uri());

            String root = getRootFromUri(msg.uri());
            requestStructure.setRoot(root);

            NettyResponseService service = new NettyResponseService(AureliusApplication.getInstance(), msg, ctx);
            NettyResponseHandler.handle(service, requestStructure);
        }
    }

    private String getRootFromUri(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "/";
        }

        return uri.split("\\?")[0];
    }
}
