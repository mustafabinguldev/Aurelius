package tech.bingulhan.webserver.response;

import tech.bingulhan.webserver.response.impl.GetResponseMvcHandler;

public interface ResponseHandler {

     void handleResponse(ResponseService service, RequestStructure structure);

     static void handle(ResponseService service, RequestStructure structure) {
          new GetResponseMvcHandler().handleResponse(service, structure);
     }

}
