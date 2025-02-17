package tech.bingulhan.webserver.response.impl.mvc;

import tech.bingulhan.webserver.app.ContainerStructure;
import tech.bingulhan.webserver.app.PageStructure;
import tech.bingulhan.webserver.response.RequestStructure;
import tech.bingulhan.webserver.response.ResponseHandler;
import tech.bingulhan.webserver.response.ResponseService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.*;

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

                for (int i = 0; i < containerStructures.size(); i++) {
                    for (ContainerStructure containerStructure : containerStructures) {
                        Pattern pattern = Pattern.compile("<container\\." + containerStructure.getName() + "([^>]*)></container\\." + containerStructure.getName() + ">");
                        Matcher matcher = pattern.matcher(htmlData);
                        if (matcher.find()) {
                            String attributesString = matcher.group(1);
                            Map<String, String> attributes = new HashMap<>();
                            Pattern attrPattern = Pattern.compile("(\\w+)=\"(.*?)\"");
                            Matcher attrMatcher = attrPattern.matcher(attributesString);
                            while (attrMatcher.find()) {
                                attributes.put(attrMatcher.group(1), attrMatcher.group(2));
                            }
                            String updatedHtml = containerStructure.getHtmlData();
                            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                                updatedHtml = updatedHtml.replace("{" + entry.getKey() + "}", entry.getValue());
                            }
                            updatedHtml = updatedHtml.replaceAll("\\{\\w+}", "");
                            htmlData = htmlData.replace(matcher.group(0), updatedHtml);
                        }
                    }
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
