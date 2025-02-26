package tech.bingulhan.webserver.app.addon;

import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.PageStructure;

import java.util.HashMap;

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

    public static void reload() {
        AureliusApplication.getInstance().getData().loadData();
    }

}
