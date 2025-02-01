package tech.bingulhan.webserver.app;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class PageDom {

    private String pageRoot;
    private String pageData;
    private String pageJsData;
    private String pageCssData;

}
