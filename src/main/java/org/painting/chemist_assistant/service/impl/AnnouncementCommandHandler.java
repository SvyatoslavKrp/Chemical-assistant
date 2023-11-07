package org.painting.chemist_assistant.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.painting.chemist_assistant.domain.Announcement;
import org.painting.chemist_assistant.domain.User;
import org.painting.chemist_assistant.repository.AnnouncementRepository;
import org.painting.chemist_assistant.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AnnouncementCommandHandler extends BaseHandler {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private boolean isCreatingAd = false;
    @Override
    public List<SendMessage> handle(Message message) {
        String messageText = message.getText();
        if (StringUtils.equals(messageText, "/announcements")) {
            return List.of(showAnnouncementOptions(message));
        } else if (StringUtils.startsWith(messageText, "view_announcements_")) {
            return List.of(showAnnouncementList(message));
        } else if (StringUtils.equals(messageText, "create_announcement_")) {
            return List.of(getSendMessage(message));
        } else if (StringUtils.startsWith(messageText, "announcement_")) {
            makeMailing(message.getChatId(), messageText);
        } else if (isCreatingAd) {
            return List.of(createAnnouncement(message));
        }
        else if (nextHandler != null) {
            return nextHandler.handle(message);
        }
        return List.of(createSendMessage(String.valueOf(message.getChatId()), "Извините, не могу обработать ваш запрос. Попробуйте снова."));
    }

    private synchronized SendMessage getSendMessage(Message message) {
        isCreatingAd = true;
        return createSendMessage(String.valueOf(message.getChatId()), "Введите Ваше оъявление");
    }

    private synchronized SendMessage createAnnouncement(Message message) {
        Long chatId = message.getChatId();
        String messageText = message.getText().replace("create_announcement_", "");
        Announcement announcement = new Announcement();
        Optional<User> optionalUser = userRepository.findById(chatId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            announcement.setUser(user);
            announcement.setDate(LocalDate.now());
            announcement.setAnnouncement(messageText);
            announcementRepository.save(announcement);
            log.info(String.format("The user (id = %d) has saved new announcement: %s", chatId, messageText));
            isCreatingAd = false;
            return new SendMessage(String.valueOf(chatId), "Ваше объявление сохранено");
        } else {
            log.error(String.format("An error occurred while saving the ad, id = %d", chatId));
            return new SendMessage(String.valueOf(chatId), "Произошла ошибка во время сохранения");
        }
    }

    private SendMessage showAnnouncementOptions(Message message) {
        Long chatId = message.getChatId();
        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(String.valueOf(chatId));
        messageToSend.setText("Выберите действие:");

        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineRow = new ArrayList<>();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();

        InlineKeyboardButton viewButton = createButton("Посмотреть список моих объявлений", "view_announcements_");
        InlineKeyboardButton createButton = createButton("Создать объявление", "create_announcement_");

        inlineRow.add(viewButton);
        inlineRow.add(createButton);
        inlineRows.add(inlineRow);

        markupLine.setKeyboard(inlineRows);
        messageToSend.setReplyMarkup(markupLine);
        return messageToSend;
    }

    private SendMessage showAnnouncementList(Message message) {
        Long chatId = message.getChat().getId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        List<Announcement> announcements = announcementRepository.findAll();
        if (announcements.isEmpty()) {
            sendMessage.setText("У вас нет объявлений");
            return sendMessage;
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
        return sendMessage;
    }

//    private SendMessage sendRepeatAnnouncement(Message message) {
//        String messageText = message.getText();
//        String announcement = messageText.replace("announcement_", "");
//        return createSendMessage(String.valueOf(message.getChatId()), announcement);
//    }

    private SendMessage createSendMessage(String chatId, String text) {
        return new SendMessage(chatId, text);
    }
    private List<SendMessage> makeMailing(Long chatId, String messageText) {
        List<User> users = userRepository.findAll();
        if (users.size() == 2) { // проблема с долбанным стартом
            return List.of(new SendMessage(String.valueOf(chatId), "Вы здесь один"));
        }
        String announcement = messageText.replace("announcement_", "");
        List<SendMessage> messages = new ArrayList<>();
        for (User userToSend : users) {
            if (!Objects.equals(userToSend.getChatId(), chatId)) {
                messages.add(new SendMessage(String.valueOf(userToSend.getChatId()), announcement));
            }
        }
        return messages;
    }
}
