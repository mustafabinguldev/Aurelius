package tech.bingulhan.webserver;

import org.jsoup.Jsoup;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class AureliusApp {

    private int port;
    private int threadSize;

    private File applicationFolder;
    private File foldersFile;

    private File settingsFile;

    private WebServer webServer;

    public AureliusApp(File file) {
        applicationFolder = file;
        init();
    }
    public void init() {
        settingsFile = new File(applicationFolder, "settings.yml");
        foldersFile = new File(applicationFolder, "app");

        if (!new File(foldersFile, "main.html").exists()) {
            System.err.println("Home page not found.");
            return;
        }
        if (!foldersFile.exists()) {
            foldersFile.mkdir();
        }
        readSettingsYml();
        start();
    }

    public void readSettingsYml() {
        try {
            Map<String, Object> settings = readYaml(settingsFile.getAbsolutePath());

            Map<String, Object> serverSettings = (Map<String, Object>) settings.get("server");
            port = (int) serverSettings.get("port");
            threadSize = (int) serverSettings.get("threadSize");

            System.out.println("Server Port: " + port);
            System.out.println("Thread Size: " + threadSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> readYaml(String filePath) throws IOException, FileNotFoundException {
        Yaml yaml = new Yaml();
        FileInputStream inputStream = new FileInputStream(filePath);
        return yaml.load(inputStream);
    }

    public void start() {
        webServer = new WebServer(port, threadSize);
        webServer.addListener((responseManager, root) -> {

            if (root.equals("/favicon.ico")) {
                 return;
            }
            File input = new File(foldersFile, root+".html");
            if (root.equals("/")) {
                input = new File(foldersFile, "main.html");
            }


            if (input.exists()) {
                try {
                    String html = new String(Files.readAllBytes(input.toPath()), StandardCharsets.UTF_8);
                    String text = Jsoup.parse(html, "UTF-8").html();
                    responseManager.addHttpData(text);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }else if (!input.equals("/")){
                responseManager.addHttpData("<h3>404</h3>");
            }
        });
        webServer.start();
    }

}
