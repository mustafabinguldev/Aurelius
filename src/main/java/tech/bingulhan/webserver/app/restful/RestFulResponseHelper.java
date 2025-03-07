package tech.bingulhan.webserver.app.restful;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.Getter;
import lombok.ToString;
import tech.bingulhan.webserver.app.restful.cookie.CookieStructure;
import tech.bingulhan.webserver.response.RequestStructure;

import java.util.HashMap;
import java.util.Random;


@ToString
public class RestFulResponseHelper {


    private ChannelHandlerContext channelHandlerContext;
    private RequestStructure requestStructure;
    private RestFulResponseStructure responseStructure;
    private FullHttpResponse fullHttpResponse;

    @Getter
    private HashMap<String, String> receivedCookies;
    public RestFulResponseHelper(ChannelHandlerContext ctx,
                                 RequestStructure requestStructure,
                                 RestFulResponseStructure responseStructure,
                                 FullHttpResponse fullHttpResponse,
                                 HashMap<String, String> receivedCookies) {
        this.channelHandlerContext = ctx;
        this.requestStructure = requestStructure;
        this.responseStructure = responseStructure;
        this.fullHttpResponse = fullHttpResponse;
        this.receivedCookies = receivedCookies;
    }

    public String getSocketAddress() {
        return channelHandlerContext.channel().remoteAddress().toString();
    }

    public void sendCookie(CookieStructure cookie) {
        StringBuilder builder = new StringBuilder();
        builder.append(cookie.getCookieName() + "=" + cookie.getCookieValue() + ";");
        cookie.getFeatures().forEach(cookieFeature -> builder.append(" " + cookieFeature.toValue() + ";"));
        fullHttpResponse.headers().add(HttpHeaderNames.SET_COOKIE, builder.toString());
    }

}
