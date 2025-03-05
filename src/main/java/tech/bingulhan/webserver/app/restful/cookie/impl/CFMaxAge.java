package tech.bingulhan.webserver.app.restful.cookie.impl;

import tech.bingulhan.webserver.app.restful.cookie.CookieFeature;

public class CFMaxAge extends CookieFeature {

    private int value;
    public CFMaxAge(int value) {
        this.value = value;
    }
    @Override
    public String toValue() {
        return "Max-Age="+value;
    }
}
