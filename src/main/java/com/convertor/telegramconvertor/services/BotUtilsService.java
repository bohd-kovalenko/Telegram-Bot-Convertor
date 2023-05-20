package com.convertor.telegramconvertor.services;

import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface BotUtilsService {
    boolean messageContainText(Update update);

    boolean messageContainPhoto(Update update);

    boolean isUpdateUsersMessageEqualsText(Update update, String text);

    SendDocument convertPhotoSizesToPDF(List<BotApiObject> photos);

    boolean messageContainUncompressedPhoto(Update update);
}
