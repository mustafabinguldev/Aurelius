package tech.bingulhan.webserver.response.impl.mvc;

import java.util.Map;

public class MvcPlaceHolderService {

    public static String replacePlaceholders(String template, Map<String, String> valuesMap) {
        String result = template;
        for (Map.Entry<String, String> entry : valuesMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
