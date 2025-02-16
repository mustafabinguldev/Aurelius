package tech.bingulhan.webserver.response.impl.mvc;

import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.ContainerStructure;
import tech.bingulhan.webserver.app.PageStructure;
import tech.bingulhan.webserver.response.RequestStructure;
import tech.bingulhan.webserver.response.ResponseHandler;
import tech.bingulhan.webserver.response.ResponseService;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetResponseMvcHandler implements ResponseHandler {
    @Override
    public void handleResponse(ResponseService service, RequestStructure structure) {
        if (structure.getMethod().equals("GET")) {

            if (structure.getRoot().equals("/favicon.ico")) {
                service.add("HTTP/1.1 400\r\n");
                service.add("Content-Type: text/html; charset=UTF-8\r\n");
                service.add("Connection: close\r\n");
                service.add("\r\n");
            }

            if (service.getApplication().getData().getPages().containsKey(structure.getRoot())) {

                service.add("HTTP/1.1 200 OK\r\n");
                service.add("Content-Type: text/html; charset=UTF-8\r\n");
                service.add("Connection: close\r\n");
                service.add("\r\n");

                PageStructure dom = service.getApplication().getData().getPages().get(structure.getRoot());
                String cssData = dom.getPageCssData();
                String jssData = dom.getPageJsData();
                String htmlData = dom.getPageData();

                List<ContainerStructure> containerStructures = service.getApplication().getData().getContainerStructures();
                for (ContainerStructure containerStructure : containerStructures) {
                    htmlData = htmlData.replaceAll("<container\\."+containerStructure.getName()+"></container\\."+containerStructure.getName()+">", containerStructure.getHtmlData());
                }

                String mergedData = MvcPlaceHolderService.replacePlaceholders(htmlData, service.getApplication().getData().getPlaceholders()).replace("</head>",
                        "<style>\n" + cssData + "\n</style>\n" +
                                "<script>\n" + jssData + "\n</script>\n</head>");
                service.addHttpData(mergedData);
                service.down();

            }else{
                service.addHttpData("<h1>404</h1>");
            }
        }

    }
}
