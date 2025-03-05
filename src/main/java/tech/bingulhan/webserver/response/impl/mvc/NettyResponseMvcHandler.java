package tech.bingulhan.webserver.response.impl.mvc;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import tech.bingulhan.webserver.app.mvc.ContainerStructure;
import tech.bingulhan.webserver.app.mvc.PageStructure;
import tech.bingulhan.webserver.response.NettyResponseHandler;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NettyResponseMvcHandler implements NettyResponseHandler {


    @Override
    public void handleResponse(NettyResponseService service, RequestStructure structure) {

        if (service.getApplication().getData().getPages().containsKey(structure.getRoot())) {

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

            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.copiedBuffer(mergedData, CharsetUtil.UTF_8));

            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            service.getCtx().writeAndFlush(response).addListener(io.netty.channel.ChannelFutureListener.CLOSE);

        }else{

            if (service.getApplication().getData().getPages().containsKey("/404")) {
                PageStructure dom = service.getApplication().getData().getPages().get("/404");
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

                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(mergedData, CharsetUtil.UTF_8));

                response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
                service.getCtx().writeAndFlush(response).addListener(io.netty.channel.ChannelFutureListener.CLOSE);

            }else{
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                        Unpooled.copiedBuffer("<html><body>404</body></html>", CharsetUtil.UTF_8));

                response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
                service.getCtx().writeAndFlush(response).addListener(io.netty.channel.ChannelFutureListener.CLOSE);

            }

        }
    }
}
