package tech.bingulhan.webserver.response;

import tech.bingulhan.webserver.response.impl.media.NettyResponseMediaHandler;
import tech.bingulhan.webserver.response.impl.mvc.NettyResponseMvcHandler;
import tech.bingulhan.webserver.response.impl.react.NettyResponseReactHandler;
import tech.bingulhan.webserver.response.impl.restful.NettyRestFulHandler;

public enum NettyResponseType {

    PAGE("", new NettyResponseMvcHandler()),
    MEDIA("public", new NettyResponseMediaHandler()),

    RESTFUL("api", new NettyRestFulHandler()),

    // Only used as the fallback when a React build exists; never matched by path segment.
    REACT(null, new NettyResponseReactHandler());
    ;
    public String path;

    public NettyResponseHandler handler;
    NettyResponseType(String path, NettyResponseHandler handler) {
        this.path = path;
        this.handler = handler;
    }

}
