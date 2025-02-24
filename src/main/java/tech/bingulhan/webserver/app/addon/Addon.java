package tech.bingulhan.webserver.app.addon;

public abstract class Addon {

    public abstract String getAddonName();

    public abstract void onEnable();

    public abstract void onDisable();
}

