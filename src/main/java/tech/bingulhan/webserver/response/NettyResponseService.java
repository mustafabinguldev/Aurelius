package tech.bingulhan.webserver.response;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Getter;
import tech.bingulhan.webserver.app.AureliusApplication;

@Getter
public class NettyResponseService {

    private FullHttpRequest request;

    private ChannelHandlerContext ctx;

    private AureliusApplication application;

    public NettyResponseService(AureliusApplication application, FullHttpRequest request, ChannelHandlerContext ctx) {
        this.application = application;
        this.request = request;
        this.ctx = ctx;
    }
}
