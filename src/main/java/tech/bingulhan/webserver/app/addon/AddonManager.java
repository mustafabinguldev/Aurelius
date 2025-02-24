package tech.bingulhan.webserver.app.addon;

import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.PageStructure;

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

}
