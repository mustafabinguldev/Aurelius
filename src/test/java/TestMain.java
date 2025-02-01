import tech.bingulhan.webserver.app.AureliusApp;

import java.io.File;

public class TestMain {

    public static void main(String[] args) {
        String workingDirectory = new File("C:\\Users\\bingu\\OneDrive\\Masaüstü\\Darkland Network\\Darkland Plugins\\genel eklentiler\\Aurelius\\src\\test\\resources").getAbsolutePath();
        new AureliusApp(new File(workingDirectory));
    }

}
