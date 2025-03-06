package tech.bingulhan.webserver.response.impl.restful;

import lombok.Getter;
import tech.bingulhan.webserver.response.impl.restful.impl.DeleteRestFulResponseHandler;
import tech.bingulhan.webserver.response.impl.restful.impl.GetRestFulResponseHandler;
import tech.bingulhan.webserver.response.impl.restful.impl.PostRestFulResponseHandler;
import tech.bingulhan.webserver.response.impl.restful.impl.PutRestFulResponseHandler;

public enum RestFulRequestType {

    GET(new GetRestFulResponseHandler()), POST(new PostRestFulResponseHandler()), DELETE(new DeleteRestFulResponseHandler()), PUT(new PutRestFulResponseHandler());

    @Getter
    private RestFulResponseHandler handler;

    RestFulRequestType(RestFulResponseHandler handler) {
        this.handler = handler;
    }
}
