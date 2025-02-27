package tech.bingulhan.webserver.app;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class AureliusApplicationPathData {

    private AureliusApplicationData data;

    private File containersFolder;

    private File foldersFile;

    private File settingsFile;
    private File placeholdersFile;

    private boolean isLoad = false;

    public AureliusApplicationPathData(AureliusApplicationData data) {
        this.data = data;
        init();
    }

    private void init() {
        settingsFile = new File(data.getApplication().getApplicationFolder(), "settings.yml");
        placeholdersFile = new File(data.getApplication().getApplicationFolder(), "placeholders.yml");
        containersFolder = new File(data.getApplication().getApplicationFolder(), "containers");

        if (!settingsFile.exists()) {

            System.out.println("settings.yml not found. Loading now..");
            try {
                settingsFile.createNewFile();

                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                Yaml yaml = new Yaml(options);

                Map<String, Object> serverConfig = new HashMap<>();
                Map<String, Object> dataC = new HashMap<>();
                dataC.put("port", 8080);
                dataC.put("threadSize", 3);
                dataC.put("ui", true);

                serverConfig.put("server", dataC);

                try (FileWriter writer = new FileWriter(settingsFile)) {
                    yaml.dump(serverConfig, writer);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (!placeholdersFile.exists()) {
            System.out.println("placeholders.yml not found. Loading now..");
            try {
                placeholdersFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        foldersFile = new File(data.getApplication().getApplicationFolder(), "app");

        if (!foldersFile.exists()) {
            System.out.println("App folder not found. Loading now..");
            foldersFile.mkdir();

        }

        File mainHtml = new File(foldersFile, "main.html");
        if (!mainHtml.exists()) {
            System.out.println("Home page not found. Loading now..");
            try {
                mainHtml.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        isLoad = true;
    }
}
