package tech.bingulhan.webserver.response;


import java.net.Socket;

public interface ResponseHandler {

     void handleResponse(ResponseService service, RequestStructure structure);

     static void handle(ResponseService service, RequestStructure structure) {
          String[] paths = structure.getRoot().split("/");
          if (paths.length>1) {
               for (ResponseType type: ResponseType.values()) {
                    if (type.path.equals(paths[1])) {
                         type.handler.handleResponse(service, structure);
                         return;
                    }
               }
          }

          ResponseType.PAGE.handler.handleResponse(service,structure);


     }

}
