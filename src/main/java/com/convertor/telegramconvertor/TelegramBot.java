package com.convertor.telegramconvertor;

import com.convertor.telegramconvertor.enums.ImageType;
import com.convertor.telegramconvertor.services.BotUtilsService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.MultiValueMap;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private static final String START_MESSAGE = "/start";
    private static final String HISTORY_CLEANING_MESSAGE = "ClearHistory";
    private static final String CONVERSION_READY_MESSAGE = "Convert2PDF";

    private final BotUtilsService botUtilsService;
    private final ReplyKeyboardMarkup replyKeyboardMarkup;
    private final MultiValueMap<String, BotApiObject> photoStorage;
    private final String botName;
    private final String botToken;

    @Override
    public void onUpdateReceived(Update update) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        if (botUtilsService.messageContainPhoto(update)) {
            photosMessageHandler(chatId, update, ImageType.COMPRESSED);
        }
        if (botUtilsService.messageContainText(update)) {
            textMessageHandler(chatId, update);
        }
        if (botUtilsService.messageContainUncompressedPhoto(update)) {
            photosMessageHandler(chatId, update, ImageType.UNCOMPRESSED);
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @SneakyThrows
    private void sendMessage(String chatId, String messageText, boolean attachKeyboard) {
        SendMessage message = new SendMessage(chatId, messageText);
        if (attachKeyboard) message.setReplyMarkup(replyKeyboardMarkup);
        execute(message);
    }

    @SneakyThrows
    private void sendDocument(String chatId, SendDocument document) {
        document.setChatId(chatId);
        execute(document);
        document.getFile().getNewMediaFile().delete();
    }

    private void photosMessageHandler(String chatId, Update update, ImageType type) {
        switch (type) {
            case COMPRESSED -> savePhotoToStorage(chatId, update.getMessage().getPhoto());
            case UNCOMPRESSED -> saveUncompressedPhotoToStorage(chatId, update.getMessage().getDocument());
        }
    }

    private void textMessageHandler(String chatId, Update update) {
        if (botUtilsService.isUpdateUsersMessageEqualsText(update, START_MESSAGE)) {
            sendMessage(chatId,
                    "This bot is designed to convert photos to PDF files. It is fully open-sourced and you can see code here. Please add photos into the chat to start interacting",
                    true);
        } else if (botUtilsService.isUpdateUsersMessageEqualsText(update, HISTORY_CLEANING_MESSAGE)) {
            photoStorage.remove(chatId);
            sendMessage(chatId,
                    "Your photos in cache has been deleted. Please add photos into the chat to convert it to PDF",
                    true);
        } else if (botUtilsService.isUpdateUsersMessageEqualsText(update, CONVERSION_READY_MESSAGE)) {
            sendDocument(chatId, botUtilsService.convertPhotoSizesToPDF(photoStorage.get(chatId)));
            photoStorage.remove(chatId);
        }
    }

    private void savePhotoToStorage(String chatId, List<PhotoSize> photos) {
        List<BotApiObject> photosByChat = photoStorage.get(chatId);
        PhotoSize theQualiestPhoto = extractTheQualiestPicture(photos);
        if (photosByChat == null) {
            photosByChat = new ArrayList<>();
            photosByChat.add(theQualiestPhoto);
            photoStorage.put(chatId, photosByChat);
        } else {
            photosByChat.add(theQualiestPhoto);
        }
    }

    private void saveUncompressedPhotoToStorage(String chatId, Document photo) {
        List<BotApiObject> photosByChat = photoStorage.get(chatId);
        if (photosByChat == null) {
            photosByChat = new ArrayList<>();
            photosByChat.add(photo);
            photoStorage.put(chatId, photosByChat);
        } else {
            photosByChat.add(photo);
        }
    }

    private PhotoSize extractTheQualiestPicture(List<PhotoSize> photos) {
        return photos
                .stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElseThrow(() -> new RuntimeException("No photos provided to save"));
    }
}
