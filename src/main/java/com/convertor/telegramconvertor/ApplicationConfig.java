package com.convertor.telegramconvertor;

import com.convertor.telegramconvertor.services.BotUtilsService;
import com.convertor.telegramconvertor.services.impl.BotUtilsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    @Value("${telegram.bot.name}")
    private String botName;
    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public ReplyKeyboardMarkup replyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Convert2PDF");
        row.add("ClearHistory");
        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @SneakyThrows
    public TelegramBotsApi telegramBotsApi() {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(
                botUtilsService(),
                replyKeyboardMarkup(),
                photoStorage(),
                botName,
                botToken);
    }

    @SneakyThrows
    @Bean
    public BotSession botSession() {
        return telegramBotsApi().registerBot(telegramBot());
    }

    @Bean
    public BotUtilsService botUtilsService() {
        return new BotUtilsServiceImpl(restTemplate(), botToken);
    }

    @Bean
    public MultiValueMap<String, BotApiObject> photoStorage() {
        return new LinkedMultiValueMap<>();
    }
}
