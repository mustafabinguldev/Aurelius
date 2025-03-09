package tech.bingulhan.webserver.app.restful;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tech.bingulhan.webserver.app.restful.cookie.CookieStructure;
import tech.bingulhan.webserver.response.impl.restful.RestFulRequestType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class RestFulResponseStructure {

    private String root;
    private RestFulResponse restFulResponse;
    private RestFulRequestType requestType;
    private List<CookieStructure> cookies = new ArrayList<>();


    public static class Builder{

        private String root;

        private RestFulResponse response;

        private RestFulRequestType requestType;

        private List<CookieStructure> cookies = new ArrayList<>();

        public Builder(String root) {
            this.root = root;
        }

        public Builder setRestFulResponse(RestFulResponse response) {
            this.response = response;
            return this;
        }

        public Builder setRequestType(RestFulRequestType type) {
            this.requestType = type;
            return this;
        }

        public Builder setCookies(List<CookieStructure> cookies) {
            this.cookies = cookies;
            return this;
        }

        public RestFulResponseStructure build() {
            RestFulResponseStructure structure = new RestFulResponseStructure();
            structure.setRequestType(this.requestType);
            structure.setRoot(this.root);
            structure.setRestFulResponse(this.response);
            structure.setCookies(this.cookies);
            return structure;
        }
    }

    public interface RestFulResponse<R,B> {
       R response(B o, RestFulResponseHelper helper);
       B convert(String bodyJson) throws Exception;

    }

}
