package tech.bingulhan.webserver.app;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.io.IOException;

@Getter
@Setter
@ToString
public class AureliusApplicationPathData {

    private AureliusApplicationData data;

    private File containersFolder;

    private File foldersFile;

    private File settingsFile;
    private File placeholdersFile;

    public AureliusApplicationPathData(AureliusApplicationData data) {
        this.data = data;
        init();
    }

    private void init() {
        settingsFile = new File(data.getApplication().getApplicationFolder(), "settings.yml");
        placeholdersFile = new File(data.getApplication().getApplicationFolder(), "placeholders.yml");
        containersFolder = new File(data.getApplication().getApplicationFolder(), "containers");

        if (!settingsFile.exists()) {
            System.err.println("settings.yml not found.");
            return;
        }

        if (!placeholdersFile.exists()) {
            System.err.println("placeholders.yml not found.");
            try {
                placeholdersFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        foldersFile = new File(data.getApplication().getApplicationFolder(), "app");
        if (!foldersFile.exists()) {
            System.err.println("App folder not found.");
            return;
        }

        if (!new File(foldersFile, "main.html").exists()) {
            System.err.println("Home page not found.");
            return;
        }
    }
}
