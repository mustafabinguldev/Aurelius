package tech.bingulhan.webserver.app.restful.cookie.impl;

import tech.bingulhan.webserver.app.restful.cookie.CookieFeature;

public class CFSecure extends CookieFeature {
    @Override
    public String toValue() {
        return "Secure";
    }
}
