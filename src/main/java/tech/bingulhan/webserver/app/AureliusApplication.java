package tech.bingulhan.webserver.app;

import jdk.internal.net.http.common.Log;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;
import tech.bingulhan.webserver.app.addon.Addon;
import tech.bingulhan.webserver.app.addon.AddonCompiler;
import tech.bingulhan.webserver.app.addon.iml.FileAddonCompiler;
import tech.bingulhan.webserver.app.ui.ApplicationUI;
import tech.bingulhan.webserver.server.HttpNettyServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class AureliusApplication {

    @Getter
    private File applicationFolder;

    @Getter
    private int port;
    private int threadSize;

    @Getter
    private boolean ui = false;

    @Getter
    private AureliusApplicationData data;

    @Getter
    private ApplicationUI applicationUI;

    private static AureliusApplication instance;

    private HttpNettyServer server;

    @Getter
    private List<Addon> addons;

    @Getter
    private List<RestFulResponseStructure> restFulResponseStructures;

    @Getter
    private AddonCompiler addonCompiler;


    public static synchronized AureliusApplication getInstance() {
        return instance;
    }

    public void stop() {
        server.shutdown();
    }


    public AureliusApplication(File file, String[] args) {

        instance = this;
        if (!file.exists()){
            System.err.println("The home directory could not be read.");
            return;
        }

        restFulResponseStructures = new ArrayList<>();
        addonCompiler = new FileAddonCompiler();
        applicationUI = new ApplicationUI();



        applicationFolder = file;

        addons = new ArrayList<>();
        data = new AureliusApplicationData(this);


        if (data.isLoad()) {
            init(args);
         }



    }

    public void init(String[] args) {
        readSettingsYml();
        readPlaceholders();
        start(args);
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


            System.out.println("Number of placeholders registered: "+data.getPlaceholders().size());

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
            ui = (Boolean) serverSettings.get("ui");


            System.out.println("Server port: "+port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Map<String, Object> readYaml(String filePath) throws IOException {
        Yaml yaml = new Yaml();
        FileInputStream inputStream = new FileInputStream(filePath);
        return yaml.load(inputStream);
    }

    public void start(String[] args) {
        server= new HttpNettyServer(this, args);
        server.start();
    }




}
