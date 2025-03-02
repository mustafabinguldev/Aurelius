package tech.bingulhan.webserver.app;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jsoup.Jsoup;
import tech.bingulhan.webserver.app.addon.Addon;

import javax.swing.plaf.metal.MetalIconFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
public class AureliusApplicationData {

    private AureliusApplication application;

    private List<MediaStructure> mediaStructures;
    private List<ContainerStructure> containerStructures;

    private  HashMap<String, PageStructure> pages;
    private HashMap<String, String> placeholders;


    private int port;
    private int threadSize;

    private AureliusApplicationPathData pathData;

    private boolean isLoad = false;

    public AureliusApplicationData(AureliusApplication application) {
        this.application = application;

        pathData = new AureliusApplicationPathData(this);

        if (pathData.isLoad()) {
            init();
        }

    }

    public void loadData() {
        pages = new HashMap<>();
        mediaStructures = new ArrayList<>();
        placeholders = new HashMap<>();
        containerStructures = new ArrayList<>();

        loadContainers();
        loadMediaFiles();
        loadPages();


        try {
            readPageData("/","main",new File(pathData.getFoldersFile(),
                            "main.html"), new File(pathData.getFoldersFile(), "main.css"),
                    new File(pathData.getFoldersFile(),"main.js"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        AureliusApplication.getInstance().getAddons().stream().forEach(Addon::onDisable);
        AureliusApplication.getInstance().getAddons().clear();
        AureliusApplication.getInstance().getAddonCompiler().doCompileAllAddons();

    }

    private void init() {
        loadData();
        isLoad = true;
    }


    private void loadContainers() {

        if (!pathData.getContainersFolder().exists()) {
            System.out.println("Containers folder not found.");
            pathData.getContainersFolder().mkdir();
        }else {
            if (Objects.requireNonNull(pathData.getContainersFolder().listFiles()).length>0) {
                List<File> htmlFiles = Arrays.stream(Objects.requireNonNull(pathData.getContainersFolder().listFiles()))
                        .filter(file -> file.getName().endsWith(".html"))
                        .collect(Collectors.toList());

                htmlFiles.forEach(file -> {

                    String htmlFileText = null;
                    try {
                        htmlFileText = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                        String html = Jsoup.parse(htmlFileText, "UTF-8").html();
                        String containerName = file.getName().substring(0, file.getName().lastIndexOf("."));
                        containerStructures.add(new ContainerStructure(containerName, html));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
            }
        }

        System.out.println("Number of registered containers: "+containerStructures.size());
    }

    private void loadMediaFiles() {
        File publicFolder = new File(application.getApplicationFolder(), "public");


        if (publicFolder.exists() && publicFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(publicFolder.listFiles())) {
                if (file.isFile()) {
                    String name = file.getName();
                    String path = file.getAbsolutePath();
                    MediaStructure structure = new MediaStructure(name, path);
                    mediaStructures.add(structure);
                }
            }
        }

        System.out.println("Number of media recorded: "+mediaStructures.size());

    }

    private void loadPages() {
        for (File folder : Objects.requireNonNull(pathData.getFoldersFile().listFiles())) {
            if (folder.isDirectory()) {
                loadFolder("/", folder);
            }
        }

        System.out.println("Number of pages saved: "+getPages().size());

    }

    private void loadFolder(String localPath, File folder) {
            String pageName= folder.getName();

            File htmlFolder = new File(folder, "page.html");
            File jsFolder = new File(folder, "page.js");
            File cssFolder = new File(folder, "page.css");

            if (htmlFolder.exists() && htmlFolder.isFile()) {
                try {
                    readPageData(localPath+pageName, pageName, htmlFolder, cssFolder, jsFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

           for (File f : Objects.requireNonNull(folder.listFiles())) {
               if (f.isDirectory()) loadFolder(localPath+pageName+"/", f);
           }

    }




    private void readPageData(String rootName,String pageName,File htmlFile, File cssFile, File jsFile) throws IOException {
        if (rootName.contains("api")) {
            return;
        }

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
        pages.put(rootName, new PageStructure(pageName, html, js, css));
    }
}
