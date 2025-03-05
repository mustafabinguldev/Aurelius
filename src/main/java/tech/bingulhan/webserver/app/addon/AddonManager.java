package tech.bingulhan.webserver.app.addon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.mvc.PageStructure;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;

import java.util.HashMap;
import java.util.List;

public class AddonManager {

    public static boolean addPageData(PageStructure structure) {

        HashMap<String,PageStructure> structureList = AureliusApplication.getInstance().getData().getPages();
        if (structureList.containsKey(structure.getPageRoot())) {
            return false;
        }

        structureList.put(structure.getPageRoot(), structure);
        return true;
    }
    public static boolean addPlaceHolderData(String key, Object data) {

        if (AureliusApplication.getInstance().getData().getPlaceholders().containsKey(key)) {
            return false;
        }

        AureliusApplication.getInstance().getData().getPlaceholders().put("%"+key+"%", data.toString());
        return true;
    }

    public static boolean registerRestFulService(RestFulResponseStructure responseStructure) {

        List<RestFulResponseStructure> structureList = AureliusApplication.getInstance().getRestFulResponseStructures();
        if (structureList.stream().anyMatch(s->s.getRoot().equals(responseStructure.getRoot()))) {
            return false;
        }
        AureliusApplication.getInstance().getRestFulResponseStructures().add(responseStructure);
        return true;

    }

    public static Object convertFromBodyJson(String body, Class clazz) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(body, clazz);
    }


    public static void reload() {
        AureliusApplication.getInstance().getData().loadData();
    }

}
