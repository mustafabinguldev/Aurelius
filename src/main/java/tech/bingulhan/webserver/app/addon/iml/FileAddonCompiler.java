package tech.bingulhan.webserver.app.addon.iml;

import org.yaml.snakeyaml.Yaml;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.addon.Addon;
import tech.bingulhan.webserver.app.addon.AddonCompiler;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Objects;

public class FileAddonCompiler implements AddonCompiler {

    @Override
    public void doCompileAllAddons() {

        System.out.println("Addons: Loading..");
        AureliusApplication application = AureliusApplication.getInstance();
        File addonFolder = new File(application.getApplicationFolder(), "addons");
        if (!addonFolder.exists() || !addonFolder.isDirectory()) {
            return;
        }
        for (File file : Objects.requireNonNull(addonFolder.listFiles())) {
            if (!file.getName().endsWith(".jar")) {
                continue;
            }
            try {
                registerAddon(file);
            }catch (Exception exception){
                continue;
            }
        }

        System.out.println("Addons: "+application.getAddons().size());

    }

    private void registerAddon(File jarFile) throws Exception {
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl});


        InputStream addonYmlStream = classLoader.getResourceAsStream("addon.yml");

        Yaml yaml = new Yaml();
        Map<String, Object> addonConfig = yaml.load(addonYmlStream);

        String mainClass = (String) addonConfig.get("main");

        if (mainClass != null) {
            Class<?> addonClass = classLoader.loadClass(mainClass);

            if (Addon.class.isAssignableFrom(addonClass)) {
                Addon addonInstance = (Addon) addonClass.getDeclaredConstructor().newInstance();
                addonInstance.onEnable();
                AureliusApplication.getInstance().getAddons().add(addonInstance);
            }
        }

    }

}
