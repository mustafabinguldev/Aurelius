package tech.bingulhan.webserver.response.impl.react;

import lombok.Getter;

import java.util.Locale;

/** Content types for files commonly emitted by React build tools. */
public enum ReactContentType {
    HTML("html", "text/html; charset=UTF-8"), HTM("htm", "text/html; charset=UTF-8"),
    JS("js", "text/javascript; charset=UTF-8"), MJS("mjs", "text/javascript; charset=UTF-8"),
    CSS("css", "text/css; charset=UTF-8"), MAP("map", "application/json"),
    JSON("json", "application/json"), WEBMANIFEST("webmanifest", "application/manifest+json"),
    TXT("txt", "text/plain; charset=UTF-8"), XML("xml", "application/xml"),
    SVG("svg", "image/svg+xml"), ICO("ico", "image/x-icon"),
    PNG("png", "image/png"), JPG("jpg", "image/jpeg"), JPEG("jpeg", "image/jpeg"),
    GIF("gif", "image/gif"), WEBP("webp", "image/webp"), AVIF("avif", "image/avif"),
    WOFF("woff", "font/woff"), WOFF2("woff2", "font/woff2"), TTF("ttf", "font/ttf"), OTF("otf", "font/otf"),
    MP4("mp4", "video/mp4"), WEBM("webm", "video/webm"), MP3("mp3", "audio/mpeg"),
    WASM("wasm", "application/wasm");

    @Getter
    private final String extension;

    @Getter
    private final String contentType;

    ReactContentType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public static String of(String fileName) {
        int dot = fileName.lastIndexOf('.');
        String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        for (ReactContentType type : values()) {
            if (type.extension.equals(extension)) return type.contentType;
        }
        return "application/octet-stream";
    }
}
