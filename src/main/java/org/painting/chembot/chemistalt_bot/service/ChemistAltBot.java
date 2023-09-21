package org.painting.chembot.chemistalt_bot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.painting.chembot.chemistalt_bot.config.BotConfig;
import org.painting.chembot.chemistalt_bot.domain.Announcement;
import org.painting.chembot.chemistalt_bot.domain.Instruction;
import org.painting.chembot.chemistalt_bot.domain.Malfunction;
import org.painting.chembot.chemistalt_bot.domain.User;
import org.painting.chembot.chemistalt_bot.repository.AnnouncementRepository;
import org.painting.chembot.chemistalt_bot.repository.InstructionRepository;
import org.painting.chembot.chemistalt_bot.repository.MalfunctionRepository;
import org.painting.chembot.chemistalt_bot.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class ChemistAltBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UserRepository userRepository;
    private final AnnouncementRepository announcementRepository;
    private final InstructionRepository instructionRepository;
    private final MalfunctionRepository malfunctionRepository;
    private boolean isCreatingAd = false;
    private boolean isCreatingMalfunction = false;
    private static final String AREA1 = "AREA1";
    private static final String AREA2 = "AREA2";
    private static final String AREA3 = "AREA3";

    public ChemistAltBot(BotConfig botConfig,
                         UserRepository userRepository,
                         AnnouncementRepository announcementRepository,
                         InstructionRepository instructionRepository,
                         MalfunctionRepository malfunctionRepository) {

        this.botConfig = botConfig;
        this.userRepository = userRepository;
        this.announcementRepository = announcementRepository;
        this.instructionRepository = instructionRepository;
        this.malfunctionRepository = malfunctionRepository;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Запустить бот"));
        listOfCommands.add(new BotCommand("/information", "Информация об участках"));
        listOfCommands.add(new BotCommand("/announcements", "Объявления"));
        listOfCommands.add(new BotCommand("/instructions", "Рабочие инструкции"));
        listOfCommands.add(new BotCommand("/malfunctions", "Неисправности"));

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
                case "/announcements" -> showAnnouncementOptions(chatId);
                case "/instructions" -> showAllInstructions(chatId);
                case "/malfunctions" -> showMalfunctionsOptions(chatId);

                default -> {
                    //синхронизировать?
                    if (isCreatingAd) {
                        saveAnnouncement(chatId, message.getText());
                        sendAnnouncementToEveryone(chatId, message.getText());
                    }
                    if (isCreatingMalfunction) {

                        saveMalfunction(message.getText());

                    } else {
                        sendMessage(chatId, "Извините, но я не узнаю команду");
                    }

                }
            }

        } else if (update.hasCallbackQuery()) {

            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();


            switch (data) {

                case AREA1 -> sendMessage(chatId, "Информация о " + AREA1);
                case AREA2 -> sendMessage(chatId, "Информация о " + AREA2);
                case AREA3 -> sendMessage(chatId, "Информация о " + AREA3);
                case "view_announcements" -> showAnnouncementList(chatId);
                case "create_announcements" -> {
                    isCreatingAd = true;
                    sendMessage(chatId, "Введите Ваше объявление");
                }
                case "view_malfunctions" -> showMalfunctionsList(chatId);
                case "create_malfunction" -> {
                    isCreatingMalfunction = true;
                    sendMessage(chatId, "Введите название новой неисправности в формате \"Название неисправности: Описание неисправности\"");
                }

                default -> {

                    if (data.startsWith("announcement_")) {

                        String announcementToRepeat = data.replace("announcement_", "");
                        sendAnnouncementToEveryone(chatId, announcementToRepeat);
                        sendMessage(chatId, "Ваше объявление отправлено");

                    }
                    if (data.startsWith("C:\\var\\db")) {
                        sendInstruction(update.getCallbackQuery());
                    }
                    if (data.startsWith("malfunction_")) {
                        Long malfunctionId = Long.parseLong(data.replace("malfunction_", ""));
                        showMalfunctionDescription(chatId, malfunctionId);
                    }

                }
            }
        }
    }

    private void showAnnouncementList(Long chatId) {

        List<Announcement> announcements = announcementRepository.findAll();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        if (announcements.isEmpty()) {
            sendMessage.setText("У вас нет объявлений");
            executeSending(sendMessage);
            return;
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Announcement announcement : announcements) {
            InlineKeyboardButton button = createButton(announcement.getAnnouncement(), "announcement_" + announcement.getAnnouncement());
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        markup.setKeyboard(rows);


        sendMessage.setReplyMarkup(markup);
        sendMessage.setText("Список объявлений:");

        executeSending(sendMessage);

    }

    private void showAnnouncementOptions(Long chatId) {

        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(String.valueOf(chatId));
        messageToSend.setText("Выберите действие:");

        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineRow = new ArrayList<>();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();

        InlineKeyboardButton viewButton = createButton("Посмотреть список моих объявлений", "view_announcements");
        InlineKeyboardButton createButton = createButton("Создать объявление", "create_announcements");

        inlineRow.add(viewButton);
        inlineRow.add(createButton);
        inlineRows.add(inlineRow);

        markupLine.setKeyboard(inlineRows);
        messageToSend.setReplyMarkup(markupLine);

        executeSending(messageToSend);
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
            LocalDate date = LocalDate.now();

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

    @Scheduled(cron = "${crone.scheduler}")
    private void clearAnnouncement() {

        List<Announcement> allAnnouncements = announcementRepository.findAll();
        for (Announcement announcement : allAnnouncements) {

            LocalDate now = LocalDate.now();
            LocalDate oneMonth = now.plusDays(1);

            LocalDate announcementDate = announcement.getDate();

            if (announcementDate.isBefore(oneMonth)) {

                announcementRepository.delete(announcement);
                log.info("The announcement " + announcement.getAnnouncement() + " has been deleted. User: " + announcement.getUser().getUsername());
            }
        }

    }

    private void showAllInstructions(Long chatId) {

        List<Instruction> allInstructions = instructionRepository.findAll();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Instruction instruction : allInstructions) {

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(instruction.getTitle());
            button.setCallbackData(instruction.getPath());

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        markup.setKeyboard(rows);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(markup);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Список инструкций: ");

        executeSending(sendMessage);
    }

    private void sendInstruction(CallbackQuery callbackQuery) {

        Long chatId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();
        String username = callbackQuery.getMessage().getChat().getUserName();

        SendDocument document = new SendDocument();
        document.setChatId(String.valueOf(chatId));

        File file = new File(data);
        InputFile inputFile = new InputFile(file);

        document.setDocument(inputFile);
        document.setCaption("Ваша инструкция");

        try {
            execute(document);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        log.info("Пользователю " + username + " получил инструкцию " + data);

    }

    private void showMalfunctionsOptions(Long chatId) {

        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(String.valueOf(chatId));
        messageToSend.setText("Выберите действие:");

        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineRow = new ArrayList<>();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();

        InlineKeyboardButton viewButton = createButton("Посмотреть список неисправностей", "view_malfunctions");
        InlineKeyboardButton reportButton = createButton("Сообщить о новой неисправности", "create_malfunction");

        inlineRow.add(viewButton);
        inlineRow.add(reportButton);
        inlineRows.add(inlineRow);

        markupLine.setKeyboard(inlineRows);
        messageToSend.setReplyMarkup(markupLine);

        executeSending(messageToSend);

    }

    private void showMalfunctionsList(Long chatId) {
        List<Malfunction> malfunctions = malfunctionRepository.findAll();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Malfunction malfunction : malfunctions) {
            InlineKeyboardButton button = createButton(malfunction.getTitle(), "malfunction_" + malfunction.getId());
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        markup.setKeyboard(rows);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(markup);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Список неисправностей:");

        executeSending(sendMessage);
    }

    private void showMalfunctionDescription(Long chatId, Long malfunctionId) {

        Optional<Malfunction> optionalMalfunction = malfunctionRepository.findById(malfunctionId);

        if (optionalMalfunction.isPresent()) {
            Malfunction malfunction = optionalMalfunction.get();
            SendMessage messageToSend = new SendMessage();
            messageToSend.setChatId(String.valueOf(chatId));
            messageToSend.setText("Описание неисправности:\n" + malfunction.getDescription());
            executeSending(messageToSend);
        } else {
            log.error("Неисправность с id " + malfunctionId + " не найдена");
        }

    }

    private void saveMalfunction(String messageText) {

        String[] parts = splitMalfunction(messageText);
        Malfunction malfunction = new Malfunction();
        malfunction.setTitle(parts[0]);
        malfunction.setDescription(parts[1]);
        malfunction.setDate(LocalDate.now());

        malfunctionRepository.save(malfunction);

        isCreatingMalfunction = false;

        log.info("Неисправность " + malfunction.getTitle() + " создана");
    }

    private String[] splitMalfunction(String messageText) {
        return messageText.split(":");
    }

    private void sendMessage(Long chatId, String textToSend) {

        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(String.valueOf(chatId));
        messageToSend.setText(textToSend);

        userRepository.findById(chatId).ifPresent(user -> log.info("Ответ пользователю " + user.getUsername()));

        executeSending(messageToSend);
    }

    private void executeSending(SendMessage messageToSend) {

        try {
            execute(messageToSend);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

}
