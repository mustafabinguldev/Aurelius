package tech.bingulhan.webserver.response;

import tech.bingulhan.webserver.response.impl.GetResponseMediaHandler;
import tech.bingulhan.webserver.response.impl.GetResponseMvcHandler;

public enum ResponseType {

    PAGE("", new GetResponseMvcHandler()),
    MEDIA("public", new GetResponseMediaHandler());


    public String path;

    public ResponseHandler handler;
    ResponseType(String path, ResponseHandler handler) {
        this.path = path;
        this.handler = handler;
    }
}
