package tech.bingulhan.webserver.app.restful.cookie.impl;

import tech.bingulhan.webserver.app.restful.cookie.CookieFeature;

public class CFPath extends CookieFeature {

    private String value;
    public CFPath(String value) {
        this.value = value;
    }
    @Override
    public String toValue() {
        return "Path="+value;
    }
}
