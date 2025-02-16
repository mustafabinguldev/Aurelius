package tech.bingulhan.webserver.app;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;
import tech.bingulhan.webserver.server.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class AureliusApplication {

    @Getter
    private File applicationFolder;

    private int port;
    private int threadSize;

    @Getter
    private AureliusApplicationData data;

    public AureliusApplication(File file) {

        data = new AureliusApplicationData(this);

        if (!file.exists()){
            System.err.println("The home directory could not be read.");
            return;
        }

        applicationFolder = file;
        init();
    }

    public void init() {
        readSettingsYml();
        readPlaceholders();
        start();
    }

    public void readPlaceholders() {
        try {
            Map<String, Object> placeholders = readYaml(data.getPathData().getPlaceholdersFile().getAbsolutePath());

            if (placeholders == null) {
                return;
            }
            if (!placeholders.isEmpty()) {
                placeholders.keySet().forEach(placeholder -> {
                    data.getPlaceholders().put("%"+placeholder+"%", placeholders.get(placeholder).toString());
                });
            }


            System.out.println("Placeholders: " + data.getPlaceholders().size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void readSettingsYml() {
        try {
            Map<String, Object> settings = readYaml(getData().getPathData().getSettingsFile().getAbsolutePath());

            Map<String, Object> serverSettings = (Map<String, Object>) settings.get("server");
            port = (int) serverSettings.get("port");
            threadSize = (int) serverSettings.get("threadSize");

            System.out.println("Server Port: " + port);
            System.out.println("Thread Size: " + threadSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Map<String, Object> readYaml(String filePath) throws IOException {
        Yaml yaml = new Yaml();
        FileInputStream inputStream = new FileInputStream(filePath);
        return yaml.load(inputStream);
    }

    public void start() {
        HttpServer webServer = new HttpServer(this,port);
        webServer.start();
    }


}
