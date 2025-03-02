package tech.bingulhan.webserver.app;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tech.bingulhan.webserver.response.impl.restful.RestFulRequestType;

@Getter
@Setter
@ToString
public class RestFulResponseStructure {

    private String root;
    private RestFulResponse restFulResponse;
    private RestFulRequestType requestType;


    public static class Builder{

        private String root;

        private RestFulResponse response;

        private RestFulRequestType requestType;
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

        public RestFulResponseStructure build() {
            RestFulResponseStructure structure = new RestFulResponseStructure();
            structure.setRequestType(this.requestType);
            structure.setRoot(this.root);
            structure.setRestFulResponse(this.response);
            return structure;
        }
    }

    public interface RestFulResponse<R,B> {
       R response(B o);

       B convert(String bodyJson) throws Exception;
    }

}
