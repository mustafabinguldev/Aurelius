package tech.bingulhan.webserver.app;

import org.jsoup.Jsoup;
import org.yaml.snakeyaml.Yaml;
import tech.bingulhan.webserver.server.WebServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AureliusApp {

    private int port;
    private int threadSize;

    private File applicationFolder;
    private File foldersFile;

    private File settingsFile;

    private WebServer webServer;

    private HashMap<String, PageDom> pages;

    public AureliusApp(File file) {

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

        pages = new HashMap<>();

        try {

            readPageData("/","main",new File(foldersFile,
                    "main.html"), new File(foldersFile, "main.js"),
                    new File("main.css"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        pagesLoad();
        readSettingsYml();
        start();
    }


    private void pagesLoad() {
        for (File folder : foldersFile.listFiles()) {
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

            if (pages.containsKey(root)) {

                PageDom dom = pages.get(root);

                String cssData = dom.getPageCssData();
                String jssData = dom.getPageJsData();
                String htmlData = dom.getPageData();
                String mergedData = htmlData.replace("</head>",
                        "<style>\n" + cssData + "\n</style>\n" +
                                "<script>\n" + jssData + "\n</script>\n</head>");

                responseManager.addHttpData(mergedData);


            }else{
                responseManager.addHttpData("<h1>404</h1>");
            }
        });
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
        pages.put(rootName, new PageDom(pageName, html, js, css));
        System.out.println("The page has been saved: "+rootName+" root: "+pageName);
    }

}
