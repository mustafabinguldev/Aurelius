package tech.bingulhan.webserver.app.restful.cookie.impl;

import tech.bingulhan.webserver.app.restful.cookie.CookieFeature;

public class CFSameSite extends CookieFeature {

    private String value;
    public CFSameSite(String value) {
        this.value = value;
    }
    @Override
    public String toValue() {
        return "SameSite="+value;
    }
}
