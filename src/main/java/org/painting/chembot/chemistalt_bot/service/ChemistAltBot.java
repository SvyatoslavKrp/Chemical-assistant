package org.painting.chembot.chemistalt_bot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.painting.chembot.chemistalt_bot.config.BotConfig;
import org.painting.chembot.chemistalt_bot.domain.Announcement;
import org.painting.chembot.chemistalt_bot.domain.User;
import org.painting.chembot.chemistalt_bot.repository.AnnouncementRepository;
import org.painting.chembot.chemistalt_bot.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ChemistAltBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UserRepository userRepository;
    private final AnnouncementRepository announcementRepository;
    private boolean isCreatingAd = false;
    private static final String AREA1 = "AREA1";
    private static final String AREA2 = "AREA2";
    private static final String AREA3 = "AREA3";
    private static final String YANDEX_DISK_LINK = "https://disk.yandex.ru/d/oBPAMXHT_vkNcQ";

    public ChemistAltBot(BotConfig botConfig, UserRepository userRepository, AnnouncementRepository announcementRepository) {

        this.botConfig = botConfig;
        this.userRepository = userRepository;
        this.announcementRepository = announcementRepository;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Запустить бот"));
        listOfCommands.add(new BotCommand("/information", "Информация об участках"));
        listOfCommands.add(new BotCommand("/create_announcement", "Создать объявление"));
        listOfCommands.add(new BotCommand("/get_all_announcements", "Мои объявления"));
        listOfCommands.add(new BotCommand("/delete_my_announcement", "Удалить мои объявления"));
        listOfCommands.add(new BotCommand("/instructions", "Рабочие инструкции"));
        listOfCommands.add(new BotCommand("/set_reminder", "Установить напоминание"));

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

            Message message = update.getMessage();
            Long chatId = message.getChatId();

            switch (message.getText()) {

                case "/start" -> startCommandReceived(message);
                case "/information" -> giveUserInformation(message);
                case "/create_announcement" -> {
                    isCreatingAd = true;
                    sendMessage(chatId, "Введите Ваше объявление");
                }
                case "/get_all_announcements" -> {

                    List<Announcement> userAnnouncements = getAllAnnouncements(chatId);

                    if (!userAnnouncements.isEmpty()) {
                        addKeyboardToAnnouncementList(userAnnouncements, chatId);
                    }

                }


                case "/instructions" -> showAllInstructions(chatId);
                case "/set_reminder" -> setReminder(chatId);

                default -> {

                    if (isCreatingAd) {
                        saveAnnouncement(chatId, message.getText());
                        sendAnnouncementToEveryone(chatId, message.getText());
                    } else {
                        sendMessage(chatId, "Извините, но я не узнаю команду");
                    }

                }
            }

        } else if (update.hasCallbackQuery()) {

            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            SendMessage messageToSend = new SendMessage();
            messageToSend.setChatId(String.valueOf(chatId));

            switch (data) {

                case AREA1 -> messageToSend.setText("Информация о " + AREA1);
                case AREA2 -> messageToSend.setText("Информация о " + AREA2);
                case AREA3 -> messageToSend.setText("Информация о " + AREA3);

                default -> {

                    if (data.startsWith("Announcement_to_delete")) {

                        String userName = update.getCallbackQuery().getMessage().getChat().getUserName();

                        deleteAnnouncement(data, userName);
                        messageToSend.setText("Ваше объявление удалено");

                    } else if (data.startsWith("announcement ")) {

                        String announcementToRepeat = data.replace("announcement ", "");
                        sendAnnouncementToEveryone(chatId, announcementToRepeat);
                        messageToSend.setText("Ваше объявление отправлено");

                    }

                }
            }

            executeSending(messageToSend);
        }
    }


    private void startCommandReceived(Message message) {

        registerUser(message);

        String userName = message.getChat().getUserName();
        Long chatId = message.getChatId();
        String answer = EmojiParser.parseToUnicode("Привет, " + userName + ", рад видеть тебя" + ":blush:");

        sendMessage(chatId, answer);

    }

    private void registerUser(Message message) {

        Long chatId = message.getChatId();

        if (userRepository.findById(chatId).isEmpty()) {

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

    private void giveUserInformation(Message message) {

        Long chatId = message.getChatId();

        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(String.valueOf(chatId));
        messageToSend.setText("Информацию о каком участке Вы хотите получить?");

        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineRow = new ArrayList<>();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();

        InlineKeyboardButton area1 = new InlineKeyboardButton();
        area1.setText("Участок 1");
        area1.setCallbackData(AREA1);

        InlineKeyboardButton area2 = new InlineKeyboardButton();
        area2.setText("Участок 2");
        area2.setCallbackData(AREA2);

        InlineKeyboardButton area3 = new InlineKeyboardButton();
        area3.setText("Участок 3");
        area3.setCallbackData(AREA3);

        inlineRow.add(area1);
        inlineRow.add(area2);
        inlineRow.add(area3);
        inlineRows.add(inlineRow);

        markupLine.setKeyboard(inlineRows);
        messageToSend.setReplyMarkup(markupLine);

        executeSending(messageToSend);

    }

    private void saveAnnouncement(Long chatId, String messageText) {


        Optional<User> optionalUser = userRepository.findById(chatId);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();
            Timestamp date = new Timestamp(System.currentTimeMillis());

            Announcement announcement = new Announcement(messageText, user, date);
            announcementRepository.save(announcement);

            isCreatingAd = false;
        }

    }

    private void sendAnnouncementToEveryone(Long chatId, String messageToSend) {

        Optional<User> userById = userRepository.findById(chatId);

        if (userById.isPresent()) {

            User user = userById.get();
            String fullAnnouncementText = "Пользователь @" + user.getUsername() + " отправил объявление:\n" + messageToSend;

            List<User> allUsers = userRepository.findAll();

            for (User userToSend : allUsers) {

//                if (!Objects.equals(chatId, userToSend.getChatId())) {

                sendMessage(userToSend.getChatId(), fullAnnouncementText);
            }
//                }

            log.info(fullAnnouncementText);
        }
    }

    private List<Announcement> getAllAnnouncements(Long chatId) {

        List<Announcement> userAnnouncements = announcementRepository.findAnnouncementByUserChatId(chatId);

        if (userAnnouncements.isEmpty()) {
            sendMessage(chatId, "У Вас нет объявлений");
            return Collections.emptyList();
        } else {
            return userAnnouncements;
        }
    }

    private void addKeyboardToAnnouncementList(List<Announcement> userAnnouncements, Long chatId) {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Announcement announcement : userAnnouncements) {

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(announcement.getAnnouncement());
            button.setCallbackData("announcement " + announcement.getAnnouncement());

            List<InlineKeyboardButton> inlineRow = new ArrayList<>();
            inlineRow.add(button);
            rows.add(inlineRow);
        }

        markup.setKeyboard(rows);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(markup);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Ваши объявления:");

        executeSending(sendMessage);

    }

    @Scheduled(cron = "${crone.scheduler}")
    private void clearAnnouncements() {

        List<Announcement> allAnnouncements = announcementRepository.findAll();
        for (Announcement announcement : allAnnouncements) {

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneMonth = now.plusMonths(1);

            Timestamp date = announcement.getDate();
            //реализую удаление даты старше месяца
        }

    }


    private void setReminder(Long chatId) {
        //String textToSend = "Пользователь @" + optionalUser.get().getUsername() + " отправил сообщение:\n" + announcementText;
        //            sendMessage(chatId, textToSend);
//        SendMessage messageToSend = new SendMessage();
//        messageToSend.setChatId(String.valueOf(chatId));
//        messageToSend.setText("Выберетие интервал");
//
//        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
//        List<InlineKeyboardButton> inlineRow = new ArrayList<>();
//        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();
//
//        InlineKeyboardButton hour = new InlineKeyboardButton();
//        hour.setText("1 час");
//        hour.setCallbackData("one_hour");
//
//        InlineKeyboardButton twoHours = new InlineKeyboardButton();
//        twoHours.setText("2 часа");
//        twoHours.setCallbackData("two_hours");
//
//        InlineKeyboardButton threeHours = new InlineKeyboardButton();
//        threeHours.setText("3 часа");
//        threeHours.setCallbackData("three_hours");
//
//        inlineRow.add(hour);
//        inlineRow.add(twoHours);
//        inlineRow.add(threeHours);
//        inlineRows.add(inlineRow);
//
//        markupLine.setKeyboard(inlineRows);
//        messageToSend.setReplyMarkup(markupLine);
//
//        executeSending(messageToSend);

    }

    private void showAllInstructions(Long chatId) {

        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(String.valueOf(chatId));
        messageToSend.setText(YANDEX_DISK_LINK);
        executeSending(messageToSend);

    }

    private void deleteAnnouncement(String announcementId, String userName) {

        String id = announcementId.replace("Announcement_to_delete ", "");
        announcementRepository.deleteById(Long.valueOf(id));

        log.info("The user " + userName + " has removed his announcement");
    }

//    private void getAnnouncementsWithKeyboard(Long chatId, AnnouncementPurpose purpose) {
//
//        List<Announcement> userAnnouncements = announcementRepository.getAnnouncementByUserChatId(chatId);
//
//        if (userAnnouncements.size() == 0) {
//
//            SendMessage sendMessage = new SendMessage();
//            sendMessage.setChatId(String.valueOf(chatId));
//            sendMessage.setText("У Вас нет объявлений");
//
//        } else {
//
//            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
//            List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();
//
//            for (Announcement announcement : userAnnouncements) {
//
//                InlineKeyboardButton button = new InlineKeyboardButton();
//                button.setText(announcement.getAnnouncement());
//                if (AnnouncementPurpose.DELETE.equals(purpose)) {
//                    button.setCallbackData("Announcement_to_delete " + announcement.getId());
//                } else if (AnnouncementPurpose.REPEAT.equals(purpose)) {
//                    button.setCallbackData("Announcement_to_repeat " + announcement.getAnnouncement());
//                }
//
//                List<InlineKeyboardButton> inlineRow = new ArrayList<>();
//                inlineRow.add(button);
//                inlineRows.add(inlineRow);
//            }
//
//            markup.setKeyboard(inlineRows);
//
//            SendMessage sendMessage = new SendMessage();
//            sendMessage.setReplyMarkup(markup);
//            sendMessage.setChatId(String.valueOf(chatId));
//            sendMessage.setText("Ваши объявления:");
//
//            executeSending(sendMessage);
//
//        }


    private void sendMessage(Long chatId, String textToSend) {

        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(String.valueOf(chatId));
        messageToSend.setText(textToSend);

        userRepository.findById(chatId).ifPresent(user -> log.info("replied to the user " + user.getUsername()));

        executeSending(messageToSend);
    }

//    @Scheduled(cron = "${crone.scheduler}")
//    private void sendAds() {
//
//        List<User> allUsers = userRepository.findAll();
//        List<Ads> allAds = adsRepository.findAll();
//
//        for (User user : allUsers) {
//            for (Ads ad : allAds) {
//                sendMessage(user.getChatId(), ad.getAd(), user.getUsername());
//            }
//        }
//
//    }

    private void executeSending(SendMessage messageToSend) {

        try {
            execute(messageToSend);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

}
