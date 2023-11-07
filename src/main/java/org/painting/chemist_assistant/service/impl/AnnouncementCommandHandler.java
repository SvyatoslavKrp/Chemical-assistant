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
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AnnouncementCommandHandler extends BaseHandler {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final boolean isCreating = false;
    @Override
    public SendMessage handle(Message message) {
        String messageText = message.getText();
        if (StringUtils.equals(messageText, "/announcements")) {
            return showAnnouncementOptions(message);
        } else if (StringUtils.startsWith(messageText, "view_announcements_")) {
            return showAnnouncementList(message);
        } else if (StringUtils.equals(messageText, "create_announcements_")) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText("Введите Ваше объявление");
            return sendMessage;
//            createAnnouncement(message);
//            Long chatId = message.getChatId();
//            SendMessage sendMessage = new SendMessage();
//            sendMessage.setChatId(String.valueOf(chatId));
//            sendMessage.setText("Ваше объявление сохранено");
//            log.info("The user (id = " + chatId + "has saved new announcement: " + messageText);
//            return sendMessage;
        }
        else if (nextHandler != null) {
            return nextHandler.handle(message);
        }
        return null;
    }

    private void createAnnouncement(Message message) {
        Long chatId = message.getChatId();
        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(String.valueOf(chatId));
        String messageText = message.getText().replace("create_announcement_", "");
        Announcement announcement = new Announcement();
        Optional<User> optionalUser = userRepository.findById(chatId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            announcement.setUser(user);
            announcement.setDate(LocalDate.now());
            announcement.setAnnouncement(messageText);
            announcementRepository.save(announcement);
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
}
