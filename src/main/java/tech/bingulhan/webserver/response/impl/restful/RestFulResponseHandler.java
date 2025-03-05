package tech.bingulhan.webserver.response.impl.restful;

import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

public interface RestFulResponseHandler {

    void handle(NettyResponseService service, RequestStructure structure, RestFulResponseStructure responseStructure);



}
