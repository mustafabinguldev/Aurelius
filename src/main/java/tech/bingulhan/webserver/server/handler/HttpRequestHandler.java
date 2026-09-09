package tech.bingulhan.webserver.server.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.EventExecutor;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.response.NettyResponseHandler;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.util.concurrent.RejectedExecutionException;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final AureliusApplication application;
    private final EventExecutor executor;

    public HttpRequestHandler() {
        this(null, null);
    }

    public HttpRequestHandler(AureliusApplication application, EventExecutor executor) {
        this.application = application;
        this.executor = executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess() || !request.uri().startsWith("/")) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            HttpUtil.setContentLength(response, 0);
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        if (executor == null) {
            handle(ctx, request);
            return;
        }
        // One ordered executor per connection preserves HTTP pipelining order.
        // Retain across the asynchronous boundary; release on every exit path.
        request.retain();
        try {
            executor.execute(() -> {
                try {
                    if (ctx.channel().isActive()) handle(ctx, request);
                } catch (Exception e) {
                    ctx.fireExceptionCaught(e);
                    ctx.close();
                } finally {
                    request.release();
                }
            });
        } catch (RejectedExecutionException e) {
            request.release();
            // A later 503 could overtake an earlier queued response. Close instead.
            ctx.close();
        }
    }

    private void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        RequestStructure structure = new RequestStructure();
        structure.setValid(true);
        structure.setMethod(request.method().name());
        structure.setUrl(request.uri());
        structure.setRoot(request.uri().split("\\?", 2)[0]);
        AureliusApplication app = application == null ? AureliusApplication.getInstance() : application;
        NettyResponseHandler.handle(new NettyResponseService(app, request, ctx), structure);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
