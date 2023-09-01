package org.painting.chembot.chemistalt_bot.service;

import lombok.extern.slf4j.Slf4j;
import org.painting.chembot.chemistalt_bot.config.BotConfig;
import org.painting.chembot.chemistalt_bot.domain.User;
import org.painting.chembot.chemistalt_bot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChemistAltBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UserRepository userRepository;
    private static final String HELP_MESSAGE = "Помощь в пути";

    public ChemistAltBot(BotConfig botConfig, UserRepository userRepository) {

        this.botConfig = botConfig;
        this.userRepository = userRepository;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Запустить бот"));
        listOfCommands.add(new BotCommand("/sat", "SAT-1"));
        listOfCommands.add(new BotCommand("/sat2", "SAT-2"));
        listOfCommands.add(new BotCommand("/trevisan", "Trevisan"));
        listOfCommands.add(new BotCommand("/help", "Помощь"));

        try {
            execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error settings bot's command list: " + e.getMessage());
        }

    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            StringBuilder username = new StringBuilder();
            username.append(update.getMessage().getChat().getFirstName())
                    .append(" ")
                    .append(update.getMessage().getChat().getLastName());

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), update.getMessage());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_MESSAGE, username.toString());
                    break;
                default:
                    sendMessage(chatId, "Sorry, the command is not recognized", username.toString());
            }

        }

    }

    private void startCommandReceived(Long chatId, String username, Message message) {

        registerUser(message);

        String answer = "Hi, " + username + ", nice to meet you";
        sendMessage(chatId, answer, username);
        log.info("replied to the user " + username);

    }

    private void registerUser(Message message) {

        if (userRepository.findById(message.getChatId()).isEmpty()) {

            Long chatId = message.getChatId();
            Chat chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user is saved " + user);

        }

    }

    private void sendMessage(Long chatId, String textToSend, String username) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        log.info("replied to the user " + username);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }

    }

}
