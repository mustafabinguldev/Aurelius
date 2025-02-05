package tech.bingulhan.webserver.app;

import org.jsoup.Jsoup;
import org.yaml.snakeyaml.Yaml;
import tech.bingulhan.webserver.server.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class AureliusApplication {

    private int port;
    private int threadSize;

    private File applicationFolder;
    private File foldersFile;

    private File settingsFile;

    private HttpServer webServer;

    public static HashMap<String, PageStructure> PAGES;

    public static List<MediaStructure> MEDIA_STRUCTURES;

    public AureliusApplication(File file) {

        if (!file.exists()){
            System.err.println("The home directory could not be read.");
            return;
        }
        applicationFolder = file;
        init();
    }
    public void init() {
        settingsFile = new File(applicationFolder, "settings.yml");

        if (!settingsFile.exists()) {
            System.err.println("settings.yml not found.");
            return;
        }

        foldersFile = new File(applicationFolder, "app");
        if (!foldersFile.exists()) {
            System.err.println("App folder not found.");
            return;
        }

        if (!new File(foldersFile, "main.html").exists()) {
            System.err.println("Home page not found.");
            return;
        }

        PAGES = new HashMap<>();
        MEDIA_STRUCTURES = new ArrayList<>();


        try {

            readPageData("/","main",new File(foldersFile,
                    "main.html"), new File(foldersFile, "main.css"),
                    new File(foldersFile,"main.js"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        pagesLoad();
        mediaFilesLoad();
        readSettingsYml();
        start();
    }


    private void mediaFilesLoad() {
        File publicFolder = new File(applicationFolder, "public");


        if (publicFolder.exists() && publicFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(publicFolder.listFiles())) {
                if (file.isFile()) {
                    String name = file.getName();
                    String path = file.getAbsolutePath();
                    MediaStructure structure = new MediaStructure(name, path);
                    MEDIA_STRUCTURES.add(structure);
                }
            }
        }

        System.out.println("Public Files: "+MEDIA_STRUCTURES.size());

    }
    private void pagesLoad() {
        for (File folder : Objects.requireNonNull(foldersFile.listFiles())) {
            if (folder.isDirectory()) {
                String pageName= folder.getName();
                File htmlFolder = new File(folder, "page.html");
                File jsFolder = new File(folder, "page.js");
                File cssFolder = new File(folder, "page.css");
                if (htmlFolder.exists() && htmlFolder.isFile()) {
                    try {
                        readPageData("/"+pageName, pageName, htmlFolder, cssFolder,jsFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
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

    public Map<String, Object> readYaml(String filePath) throws IOException {
        Yaml yaml = new Yaml();
        FileInputStream inputStream = new FileInputStream(filePath);
        return yaml.load(inputStream);
    }

    public void start() {
        webServer = new HttpServer(port, threadSize);
        webServer.start();
    }

    private void readPageData(String rootName,String pageName,File htmlFile, File cssFile, File jsFile) throws IOException {
        String htmlFileText = new String(Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8);
        String html = Jsoup.parse(htmlFileText, "UTF-8").html();

        String js = "";
        String css = "";
        if (cssFile.exists() && cssFile.isFile()) {
            String cssFileText = new String(Files.readAllBytes(cssFile.toPath()), StandardCharsets.UTF_8);
            css = Jsoup.parse(cssFileText, "UTF-8").text();
        }

        if (jsFile.exists() && jsFile.isFile()) {
            String jsFileText = new String(Files.readAllBytes(jsFile.toPath()), StandardCharsets.UTF_8);
            js = Jsoup.parse(jsFileText, "UTF-8").text();
        }
        PAGES.put(rootName, new PageStructure(pageName, html, js, css));
        System.out.println("The page has been saved: "+rootName+" root: "+pageName);
    }

}
