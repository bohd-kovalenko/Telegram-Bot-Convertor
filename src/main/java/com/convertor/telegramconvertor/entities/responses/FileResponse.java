package com.convertor.telegramconvertor.entities.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileResponse {
    @JsonAlias(value = "ok")
    private boolean ok;
    @JsonAlias(value = "result")
    private Document document;

    public String getFileUrl(String botToken) {
        return "https://api.telegram.org/file/bot" + botToken + "/" + document.filePath;
    }

    @Data
    static class Document {
        @JsonAlias(value = "file_id")
        private String fileId;
        @JsonAlias(value = "file_unique_id")
        private String fileUniqueId;
        @JsonAlias(value = "file_size")
        private String fileSize;
        @JsonAlias(value = "file_path")
        private String filePath;
    }
}
