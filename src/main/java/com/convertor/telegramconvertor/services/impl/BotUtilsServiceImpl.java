package com.convertor.telegramconvertor.services.impl;

import com.convertor.telegramconvertor.entities.responses.FileResponse;
import com.convertor.telegramconvertor.services.BotUtilsService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class BotUtilsServiceImpl implements BotUtilsService {
    private final RestTemplate restTemplate;
    private final String botToken;

    private boolean isUpdateUsersMessage(Update update) {
        if (update.getMessage() != null) {
            return update.getMessage().isUserMessage();
        }
        return false;
    }

    @Override
    public boolean messageContainText(Update update) {
        if (isUpdateUsersMessage(update) && update.getMessage().getText() != null) {
            return update.getMessage().isUserMessage();
        }
        return false;
    }

    @Override
    public boolean messageContainPhoto(Update update) {
        if (isUpdateUsersMessage(update)) {
            return update.getMessage().getPhoto() != null;
        }
        return false;
    }

    @Override
    public boolean messageContainUncompressedPhoto(Update update) {
        if (isUpdateUsersMessage(update)) {
            Document document = update.getMessage().getDocument();
            return document != null && document.getMimeType().startsWith("image");
        }
        return false;
    }

    @Override
    public boolean isUpdateUsersMessageEqualsText(Update update, String text) {
        return update.getMessage().getText().equals(text);
    }

    @SneakyThrows
    private PDDocument getPhotoFromTelegramServersInPDF(List<BotApiObject> photos) {
        PDDocument pdDocument = new PDDocument();
        PDPage newPage;
        for (BotApiObject photo : photos) {
            String fileId = photo instanceof Document ? ((Document) photo).getFileId() : ((PhotoSize) photo).getFileId();
            ResponseEntity<FileResponse> response = restTemplate.getForEntity("https://api.telegram.org/bot" + botToken + "/getFile?file_id=" + fileId
                    , FileResponse.class);
            String fileUrl = Objects.requireNonNull(response.getBody()).getFileUrl(botToken);
            File resultPhoto = File.createTempFile(UUID.randomUUID().toString(), "", new File(System.getProperty("user.dir")));
            restTemplate.execute(fileUrl, HttpMethod.GET, null, response1 -> {
                try (FileOutputStream outputStream = new FileOutputStream(resultPhoto)) {
                    StreamUtils.copy(response1.getBody(), outputStream);
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            PDImageXObject image = PDImageXObject.createFromFileByContent(resultPhoto, pdDocument);

            float newWidth = image.getWidth();
            float newHeight = image.getHeight();

            newPage = new PDPage(new PDRectangle(newWidth, newHeight));
            pdDocument.addPage(newPage);

            PDPageContentStream pdPageContentStream = new PDPageContentStream(pdDocument, newPage);
            pdPageContentStream.drawImage(image, 0, 0, newWidth, newHeight);
            pdPageContentStream.close();

            resultPhoto.delete();
        }
        return pdDocument;
    }


    @SneakyThrows
    @Override
    public SendDocument convertPhotoSizesToPDF(List<BotApiObject> photos) {
        try {
            PDDocument pdf = getPhotoFromTelegramServersInPDF(photos);
            SendDocument telegramSendableDocument = new SendDocument();
            File file = File.createTempFile(UUID.randomUUID().toString()
                    , ".pdf"
                    , new File(System.getProperty("user.dir")));
            pdf.save(file);
            telegramSendableDocument.setDocument(new InputFile(file));
            return telegramSendableDocument;
        } catch (NullPointerException e) {
            throw new RuntimeException("No photos provided. All ok");
        }
    }
}
