package org.painting.chemist_assistant.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.painting.chemist_assistant.config.BotConfig;
import org.painting.chemist_assistant.domain.Announcement;
import org.painting.chemist_assistant.domain.Workshop;
import org.painting.chemist_assistant.repository.AnnouncementRepository;
import org.painting.chemist_assistant.repository.WorkshopRepository;
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
import java.util.*;
import java.util.function.Consumer;

@Service
@Slf4j
public class ChemistAltBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
//    private final UserRepository userRepository;
    private final MessageHandler startCommandHandler;
    private final AnnouncementRepository announcementRepository;
//    private final InstructionRepository instructionRepository;
//    private final MalfunctionRepository malfunctionRepository;
    private final WorkshopRepository workshopRepository;

    private final Map<String, Consumer<Message>> commands = new HashMap<>();
    private boolean isCreatingAd = false;
    private boolean isCreatingMalfunction = false;

    public ChemistAltBot(BotConfig botConfig,
                         MessageHandler startCommandHandler,
                         MessageHandler informationCommandHandler,
                         MessageHandler announcementCommandHandler,
                         AnnouncementRepository announcementRepository,
                         WorkshopRepository workshopRepository) {

        this.botConfig = botConfig;
        this.startCommandHandler = startCommandHandler;
        this.announcementRepository = announcementRepository;
        this.workshopRepository = workshopRepository;
//        this.userRepository = userRepository;
//        this.announcementRepository = announcementRepository;
//        this.instructionRepository = instructionRepository;
//        this.malfunctionRepository = malfunctionRepository;
//        this.workshopRepository = workshopRepository;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Запустить бот"));
        listOfCommands.add(new BotCommand("/information", "Информация об участках"));
        listOfCommands.add(new BotCommand("/announcements", "Объявления"));
        listOfCommands.add(new BotCommand("/instructions", "Рабочие инструкции"));
        listOfCommands.add(new BotCommand("/malfunctions", "Неисправности"));

        startCommandHandler.setNextHandler(informationCommandHandler);
        informationCommandHandler.setNextHandler(announcementCommandHandler);

//        commands.put("/start", this::startCommandReceived);
//        commands.put("/information", this::getInfoOptions);
//        commands.put("/announcements", this::showAnnouncementOptions);
//        commands.put("/instructions", this::showAllInstructions);
//        commands.put("/malfunctions", this::showMalfunctionsOptions);

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
            SendMessage messageToSend = startCommandHandler.handle(message);
            executeSending(messageToSend);

//            Consumer<Message> handler = commands.get(messageText);
//
//            if (handler != null) {
//                handler.accept(message);
//            } else {
//                sendMessage(message.getChatId(), "Извините, но я не узнаю команду");
//            }

//            switch (message.getText()) {
//
//                case "/start" -> startCommandReceived(message);
//                case "/information" -> getInfoOptions(message);
//                case "/announcements" -> showAnnouncementOptions(message);
//                case "/instructions" -> showAllInstructions(message);
//                case "/malfunctions" -> showMalfunctionsOptions(message);
//
//                default -> {
//                    //синхронизировать?
//                    if (isCreatingAd) {
//                        saveAnnouncement(chatId, message.getText());
//                        sendAnnouncementToEveryone(chatId, message.getText());
//                    }
//                    if (isCreatingMalfunction) {
//
//                        saveMalfunction(message.getText());
//
//                    } else {
//                        sendMessage(chatId, "Извините, но я не узнаю команду");
//                    }
//
//                }
//            }


        } else if (update.hasCallbackQuery()) {

            String data = update.getCallbackQuery().getData();
            Message message = update.getCallbackQuery().getMessage();
            message.setText(data);
            SendMessage messageToSend = startCommandHandler.handle(message);
            executeSending(messageToSend);

//            if (StringUtils.startsWith(data, "workshop_description_")) {
//                String workshopId = data.replace("workshop_description_", "");
//                sendUserWorkshopInformation(chatId, Long.valueOf(workshopId));
//            }
//            if (StringUtils.equals(data, "view_announcements")) {
//                showAnnouncementList(chatId);
//            }
//            if (StringUtils.equals(data, "create_announcements")) {
//
//            }


//                case "view_announcements" -> showAnnouncementList(chatId);
//                case "create_announcements" -> {
//                    isCreatingAd = true;
//                    sendMessage(chatId, "Введите Ваше объявление");
//                }
//                case "view_malfunctions" -> showMalfunctionsList(chatId);
//                case "create_malfunction" -> {
//                    isCreatingMalfunction = true;
//                    sendMessage(chatId, "Введите название новой неисправности в формате \"Название неисправности: Описание неисправности\"");
//                }
//
//                default -> {
//
//                    if (data.startsWith("workshop_description")) {
//
//                        String workshopId = data.replace("workshop_description", "");
//                        getWorkshopInfo(Long.valueOf(workshopId), chatId);
//
//                    }
//
//                    if (data.startsWith("announcement_")) {
//
//                        String announcementToRepeat = data.replace("announcement_", "");
//                        sendAnnouncementToEveryone(chatId, announcementToRepeat);
//                        sendMessage(chatId, "Ваше объявление отправлено");
//
//                    }
//                    if (data.startsWith("C:\\var\\db")) {
//                        sendInstruction(update.getCallbackQuery());
//                    }
//                    if (data.startsWith("malfunction_")) {
//                        Long malfunctionId = Long.parseLong(data.replace("malfunction_", ""));
//                        showMalfunctionDescription(chatId, malfunctionId);
//                    }
//
//                }

        }
    }

//    private void showAnnouncementList(Long chatId) {
//
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(String.valueOf(chatId));
//
//        List<Announcement> announcements = announcementRepository.findAll();
//        if (announcements.isEmpty()) {
//            sendMessage.setText("У вас нет объявлений");
//            executeSending(sendMessage);
//            return;
//        }
//
//        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
//        for (Announcement announcement : announcements) {
//            InlineKeyboardButton button = createButton(announcement.getAnnouncement(), "announcement_" + announcement.getAnnouncement());
//            List<InlineKeyboardButton> row = new ArrayList<>();
//            row.add(button);
//            rows.add(row);
//        }
//        markup.setKeyboard(rows);
//
//        sendMessage.setReplyMarkup(markup);
//        sendMessage.setText("Список объявлений:");
//        executeSending(sendMessage);
//    }

    private void sendUserWorkshopInformation(Long chatId, Long workshopId) {
        Optional<Workshop> optionalWorkshop = workshopRepository.findById(workshopId);
        if (optionalWorkshop.isPresent()) {
            SendMessage messageToSend = new SendMessage();
            messageToSend.setChatId(String.valueOf(chatId));
            messageToSend.setText(optionalWorkshop.get().getDescription());
            executeSending(messageToSend);
            log.info("The user (id = " + chatId + " has asked information about workshop (id = " + workshopId);
        }
    }

    private void showAnnouncementOptions(Message message) {

        Long chatId = message.getChatId();

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


//    private void startCommandReceived(Message message) {
//
//        registerUser(message);
//
//        String userName = message.getChat().getUserName();
//        Long chatId = message.getChatId();
//        String answer = EmojiParser.parseToUnicode("Привет, " + userName + ", рад видеть тебя" + ":blush:");
//
//        sendMessage(chatId, answer);
//
//    }

//    private void registerUser(Message message) {
//
//        Long chatId = message.getChatId();
//
//        if (userRepository.findById(chatId).isEmpty()) {
//
//            Chat chat = message.getChat();
//            org.painting.chemist_assistant.domain.User user = new org.painting.chemist_assistant.domain.User();
//
//            user.setChatId(chatId);
//            user.setFirstName(chat.getFirstName());
//            user.setLastName(chat.getLastName());
//            user.setUsername(chat.getUserName());
//            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
//
//            userRepository.save(user);
//            log.info("user is saved " + user);
//        }
//    }

//    private void getInfoOptions(Message message) {
//
//        Long chatId = message.getChatId();
//        List<Workshop> workshops = workshopRepository.findAll();
//
//        if (workshops.isEmpty()) {
//            sendMessage(chatId, "Участков не найдено");
//            return;
//        }
//
//        SendMessage messageToSend = new SendMessage();
//        messageToSend.setChatId(String.valueOf(chatId));
//        messageToSend.setText("Информацию о каком участке Вы хотите получить?");
//
//        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
//        List<InlineKeyboardButton> inlineRow = new ArrayList<>();
//        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();
//
//        for (Workshop workshop : workshops) {
//            InlineKeyboardButton workshopButton = createButton(workshop.getName(), "workshop_description" + workshop.getId());
//            inlineRow.add(workshopButton);
//        }
//
//        inlineRows.add(inlineRow);
//
//        markupLine.setKeyboard(inlineRows);
//        messageToSend.setReplyMarkup(markupLine);
//
//        executeSending(messageToSend);
//
//        String username = message.getChat().getUserName();
//        log.info("Пользователь " + username + " id = " + chatId + " запросил информацию о линиях");
//    }

//    private void getWorkshopInfo(Long workshopId, Long chatId) {
//
//        Optional<Workshop> workshopOptional = workshopRepository.findById(workshopId);
//        if (workshopOptional.isPresent()) {
//
//            Workshop workshop = workshopOptional.get();
//            String description = workshop.getDescription();
//
//            sendMessage(chatId, description);
//            log.info("Пользователь c id = " + chatId + " запросил информацию о " + workshop.getName());
//        }
//
//    }

//    private void saveAnnouncement(Long chatId, String messageText) {
//
//        Optional<org.painting.chemist_assistant.domain.User> optionalUser = userRepository.findById(chatId);
//
//        if (optionalUser.isPresent()) {
//
//            org.painting.chemist_assistant.domain.User user = optionalUser.get();
//            LocalDate date = LocalDate.now();
//
//            Announcement announcement = new Announcement(messageText, user, date);
//            announcementRepository.save(announcement);
//
//            isCreatingAd = false;
//        }
//
//    }

//    private void sendAnnouncementToEveryone(Long chatId, String messageToSend) {
//
//        Optional<org.painting.chemist_assistant.domain.User> userById = userRepository.findById(chatId);
//
//        if (userById.isPresent()) {
//
//            org.painting.chemist_assistant.domain.User user = userById.get();
//            String fullAnnouncementText = "Пользователь @" + user.getUsername() + " отправил объявление:\n" + messageToSend;
//
//            List<org.painting.chemist_assistant.domain.User> allUsers = userRepository.findAll();
//
//            for (User userToSend : allUsers) {
//
////                if (!Objects.equals(chatId, userToSend.getChatId())) {
//
//                sendMessage(userToSend.getChatId(), fullAnnouncementText);
//            }
////                }
//
//            log.info(fullAnnouncementText);
//        }
//    }

//    @Scheduled(cron = "${crone.scheduler}")
//    private void clearAnnouncement() {
//
//        List<Announcement> allAnnouncements = announcementRepository.findAll();
//        for (Announcement announcement : allAnnouncements) {
//
//            LocalDate now = LocalDate.now();
//            LocalDate oneMonth = now.plusDays(1);
//
//            LocalDate announcementDate = announcement.getDate();
//
//            if (announcementDate.isBefore(oneMonth)) {
//
//                announcementRepository.delete(announcement);
//                log.info("The announcement " + announcement.getAnnouncement() + " has been deleted. User: " + announcement.getUser().getUsername());
//            }
//        }
//
//    }

//    private void showAllInstructions(Message message) {
//
//        Long chatId = message.getChatId();
//        List<Instruction> allInstructions = instructionRepository.findAll();
//
//        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
//
//        for (Instruction instruction : allInstructions) {
//
//            InlineKeyboardButton button = new InlineKeyboardButton();
//            button.setText(instruction.getTitle());
//            button.setCallbackData(instruction.getPath());
//
//            List<InlineKeyboardButton> row = new ArrayList<>();
//            row.add(button);
//            rows.add(row);
//        }
//
//        markup.setKeyboard(rows);
//
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setReplyMarkup(markup);
//        sendMessage.setChatId(String.valueOf(chatId));
//        sendMessage.setText("Список инструкций: ");
//
//        executeSending(sendMessage);
//    }

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

    private void showMalfunctionsOptions(Message message) {

        Long chatId = message.getChatId();

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

//    private void showMalfunctionsList(Long chatId) {
//        List<Malfunction> malfunctions = malfunctionRepository.findAll();
//
//        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
//
//        for (Malfunction malfunction : malfunctions) {
//            InlineKeyboardButton button = createButton(malfunction.getTitle(), "malfunction_" + malfunction.getId());
//            List<InlineKeyboardButton> row = new ArrayList<>();
//            row.add(button);
//            rows.add(row);
//        }
//
//        markup.setKeyboard(rows);
//
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setReplyMarkup(markup);
//        sendMessage.setChatId(String.valueOf(chatId));
//        sendMessage.setText("Список неисправностей:");
//
//        executeSending(sendMessage);
//    }
//
//    private void showMalfunctionDescription(Long chatId, Long malfunctionId) {
//
//        Optional<Malfunction> optionalMalfunction = malfunctionRepository.findById(malfunctionId);
//
//        if (optionalMalfunction.isPresent()) {
//            Malfunction malfunction = optionalMalfunction.get();
//            SendMessage messageToSend = new SendMessage();
//            messageToSend.setChatId(String.valueOf(chatId));
//            messageToSend.setText("Описание неисправности:\n" + malfunction.getDescription());
//            executeSending(messageToSend);
//        } else {
//            log.error("Неисправность с id " + malfunctionId + " не найдена");
//        }
//
//    }
//
//    private void saveMalfunction(String messageText) {
//
//        String[] parts = splitMalfunction(messageText);
//        Malfunction malfunction = new Malfunction();
//        malfunction.setTitle(parts[0]);
//        malfunction.setDescription(parts[1]);
//        malfunction.setDate(LocalDate.now());
//
//        malfunctionRepository.save(malfunction);
//
//        isCreatingMalfunction = false;
//
//        log.info("Неисправность " + malfunction.getTitle() + " создана");
//    }

    private String[] splitMalfunction(String messageText) {
        return messageText.split(":");
    }

    private void sendMessage(Long chatId, String textToSend) {

        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(String.valueOf(chatId));
        messageToSend.setText(textToSend);

//        userRepository.findById(chatId).ifPresent(user -> log.info("Ответ пользователю " + user.getUsername()));

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
