package tech.bingulhan.webserver.listeners;

import tech.bingulhan.webserver.response.ResponseManager;


public interface HttpListener {

    void onHttpRequest(ResponseManager responseManager, String root);
}
