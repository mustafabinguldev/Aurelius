package tech.bingulhan.webserver.response.impl;

import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.PageStructure;
import tech.bingulhan.webserver.response.RequestStructure;
import tech.bingulhan.webserver.response.ResponseHandler;
import tech.bingulhan.webserver.response.ResponseService;

import java.io.IOException;
import java.net.Socket;

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

            if (AureliusApplication.PAGES.containsKey(structure.getRoot())) {

                service.add("HTTP/1.1 200 OK\r\n");
                service.add("Content-Type: text/html; charset=UTF-8\r\n");
                service.add("Connection: close\r\n");
                service.add("\r\n");

                PageStructure dom = AureliusApplication.PAGES.get(structure.getRoot());
                String cssData = dom.getPageCssData();
                String jssData = dom.getPageJsData();
                String htmlData = dom.getPageData();
                String mergedData = htmlData.replace("</head>",
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
