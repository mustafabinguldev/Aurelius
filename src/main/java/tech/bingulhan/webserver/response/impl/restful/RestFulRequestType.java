package tech.bingulhan.webserver.response.impl.restful;

import lombok.Getter;
import tech.bingulhan.webserver.response.impl.restful.impl.GetRestFulResponseHandler;
import tech.bingulhan.webserver.response.impl.restful.impl.PostRestFulResponseHandler;

public enum RestFulRequestType {

    GET(new GetRestFulResponseHandler()), POST(new PostRestFulResponseHandler());

    @Getter
    private RestFulResponseHandler handler;

    RestFulRequestType(RestFulResponseHandler handler) {
        this.handler = handler;
    }
}
