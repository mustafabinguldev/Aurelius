package tech.bingulhan.webserver.response.impl.media;

import lombok.Getter;

public enum ResponseMediaType {
    JPEG("jpeg", "image/jpeg"), JPG("jpg", "image/jpeg"),
    PNG("png", "image/png"), MP4("mp4", "video/mp4"),
    YML("yml", "application/x-yaml"), YAML("yaml", "application/x-yaml"),
    JSON("json", "application/json");

    @Getter
    private String extension;

    @Getter
    private String contentType;

    ResponseMediaType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }
}
