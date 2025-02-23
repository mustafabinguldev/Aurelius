package tech.bingulhan.webserver.response;

import tech.bingulhan.webserver.response.impl.media.NettyResponseMediaHandler;
import tech.bingulhan.webserver.response.impl.mvc.NettyResponseMvcHandler;

public enum NettyResponseType {

    PAGE("", new NettyResponseMvcHandler()),
    MEDIA("public", new NettyResponseMediaHandler());
    ;
    public String path;

    public NettyResponseHandler handler;
    NettyResponseType(String path, NettyResponseHandler handler) {
        this.path = path;
        this.handler = handler;
    }

}
