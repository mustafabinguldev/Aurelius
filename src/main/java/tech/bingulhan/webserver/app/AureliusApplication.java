package tech.bingulhan.webserver.app;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;
import tech.bingulhan.webserver.app.addon.Addon;
import tech.bingulhan.webserver.app.addon.AddonCompiler;
import tech.bingulhan.webserver.app.addon.iml.FileAddonCompiler;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.app.ui.ApplicationUI;
import tech.bingulhan.webserver.server.HttpNettyServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class AureliusApplication {

    @Getter
    private File applicationFolder;

    @Getter
    private int port;
    @Getter
    private int threadSize;
    @Getter
    private int requestThreadSize;
    @Getter
    private int maxPendingRequests;

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
        if (server != null) server.shutdown();
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

            Object section = settings == null ? null : settings.get("server");
            if (section != null && !(section instanceof Map)) {
                throw new IllegalArgumentException("server bir YAML nesnesi olmalidir");
            }
            Map<?, ?> serverSettings = section == null ? Collections.emptyMap() : (Map<?, ?>) section;
            int processors = Runtime.getRuntime().availableProcessors();
            port = integerSetting(serverSettings, "port", 8080, 0, 65535);
            threadSize = integerSetting(serverSettings, "threadSize", 0, 0, 1024);
            if (threadSize == 0) threadSize = Math.min(4, processors);
            requestThreadSize = integerSetting(serverSettings, "requestThreadSize", 0, 0, 1024);
            if (requestThreadSize == 0) requestThreadSize = Math.max(2, Math.min(8, processors));
            maxPendingRequests = integerSetting(serverSettings, "maxPendingRequests", 32, 16, 65536);
            Object uiSetting = serverSettings.get("ui");
            if (uiSetting != null && !(uiSetting instanceof Boolean)) {
                throw new IllegalArgumentException("server.ui true veya false olmalidir");
            }
            ui = Boolean.TRUE.equals(uiSetting);
            System.out.println("Server port: "+port);
        } catch (IOException e) {
            throw new IllegalStateException("settings.yml okunamadi", e);
        }
    }

    private static int integerSetting(Map<?, ?> settings, String key, int fallback, int min, int max) {
        Object value = settings.get(key);
        if (value == null) return fallback;
        if (!(value instanceof Integer || value instanceof Long)
                || ((Number) value).longValue() < min || ((Number) value).longValue() > max) {
            throw new IllegalArgumentException("server." + key + " " + min + ".." + max + " araliginda tam sayi olmalidir");
        }
        return ((Number) value).intValue();
    }

    public Map<String, Object> readYaml(String filePath) throws IOException {
        Yaml yaml = new Yaml();
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            return yaml.load(inputStream);
        }
    }

    public void start(String[] args) {
        server= new HttpNettyServer(this, args);
        server.start();
    }
}
