package tech.bingulhan.webserver.app.restful.cookie;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CookieStructure {

    private String cookieName;
    private String cookieValue;

    private List<CookieFeature> features;
}
