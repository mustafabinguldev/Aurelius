package tech.bingulhan.webserver.app.mvc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class PageStructure {

    private String pageRoot;
    private String pageData;
    private String pageJsData;
    private String pageCssData;

    public static class Builder {

        private String pageRoot;
        private String pageData;
        private String pageJsData;
        private String pageCssData;

        public Builder(String pageRoot) {
            this.pageRoot = pageRoot;
        }

        public Builder setPageHtmlData(String pageData) {
            this.pageData = pageData;
            return this;
        }

        public Builder setPageJsData(String pageJsData) {
            this.pageJsData = pageJsData;
            return this;
        }

        public Builder setPageCssData(String pageCssData) {
            this.pageCssData = pageCssData;
            return this;
        }

        public PageStructure build() {
            return new PageStructure(this.pageRoot, pageData, pageJsData, pageCssData);
        }

    }

}
