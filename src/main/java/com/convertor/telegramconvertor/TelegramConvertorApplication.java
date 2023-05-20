package com.convertor.telegramconvertor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class TelegramConvertorApplication {
    public static void main(String[] args) {
        SpringApplication.run(TelegramConvertorApplication.class, args);
    }

}
