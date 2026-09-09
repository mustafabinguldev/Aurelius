package tech.bingulhan.webserver.app;

import org.junit.Test;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ServerSettingsTest {
    @Test
    public void missingSettingsUseDefaults() throws Exception {
        AureliusApplication app = application(Collections.emptyMap());
        app.readSettingsYml();
        assertEquals(8080, app.getPort());
        assertFalse(app.isUi());
    }

    @Test
    public void negativeThreadCountIsRejected() throws Exception {
        AureliusApplication app = application(server("threadSize", -1));
        assertThrows(IllegalArgumentException.class, app::readSettingsYml);
    }

    @Test
    public void fractionalThreadCountIsRejected() throws Exception {
        AureliusApplication app = application(server("threadSize", 1.5));
        assertThrows(IllegalArgumentException.class, app::readSettingsYml);
    }

    @Test
    public void invalidPortIsRejected() throws Exception {
        AureliusApplication app = application(server("port", 65536));
        assertThrows(IllegalArgumentException.class, app::readSettingsYml);
    }

    @Test
    public void legacySettingsRemainSupported() throws Exception {
        AureliusApplication app = application(server("threadSize", 2));
        app.readSettingsYml();
        assertEquals(8080, app.getPort());
        assertFalse(app.isUi());
    }

    private Map<String, Object> server(String key, Object value) {
        Map<String, Object> server = new HashMap<>();
        server.put("port", 8080);
        server.put("ui", false);
        server.put("threadSize", 2);
        server.put(key, value);
        return Collections.singletonMap("server", server);
    }

    private AureliusApplication application(Map<String, Object> settings) throws Exception {
        AureliusApplication app = mock(AureliusApplication.class, CALLS_REAL_METHODS);
        AureliusApplicationData data = mock(AureliusApplicationData.class);
        AureliusApplicationPathData paths = mock(AureliusApplicationPathData.class);
        doReturn(data).when(app).getData();
        when(data.getPathData()).thenReturn(paths);
        when(paths.getSettingsFile()).thenReturn(new File("settings.yml"));
        doReturn(settings).when(app).readYaml(anyString());
        return app;
    }
}
