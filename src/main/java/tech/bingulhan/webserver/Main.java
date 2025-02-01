package tech.bingulhan.webserver;

import tech.bingulhan.webserver.app.AureliusApp;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        String workingDirectory = System.getProperty("user.dir");
        new AureliusApp(new File(workingDirectory));
    }
}
