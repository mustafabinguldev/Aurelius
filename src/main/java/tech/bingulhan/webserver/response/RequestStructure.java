package tech.bingulhan.webserver.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestStructure {

    private boolean isValid;

    private String method;
    private String url;
    private String root;


}
