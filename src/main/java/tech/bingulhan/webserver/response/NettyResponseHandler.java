package tech.bingulhan.webserver.response;

public interface NettyResponseHandler {


    void handleResponse(NettyResponseService service, RequestStructure structure);

    static void handle(NettyResponseService service, RequestStructure structure) {
        String[] paths = structure.getRoot().split("/");
        if (paths.length>1) {
            for (NettyResponseType type: NettyResponseType.values()) {
                if (type.path.equals(paths[1])) {
                    type.handler.handleResponse(service, structure);
                    return;
                }
            }
        }

        NettyResponseType.PAGE.handler.handleResponse(service,structure);


    }
}
