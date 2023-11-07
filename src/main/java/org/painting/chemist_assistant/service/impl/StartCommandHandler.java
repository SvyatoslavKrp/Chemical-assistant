package org.painting.chemist_assistant.service.impl;

import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import org.painting.chemist_assistant.domain.User;
import org.painting.chemist_assistant.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StartCommandHandler extends BaseHandler {

    private final UserRepository userRepository;

    @Override
    public List<SendMessage> handle(Message message) {

        Long chatId = message.getChatId();
        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(String.valueOf(chatId));

        if (message.getText().equals("/start")) {
            String answer = registerUser(message);
            messageToSend.setText(answer);

        } else if (nextHandler != null) {
            return nextHandler.handle(message);
        }
        return List.of(messageToSend);
    }

    private String registerUser(Message message) {

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
            log.info("user is saved " + user.getFirstName() + " " + user.getLastName() + ", id: " + user.getChatId());
            return EmojiParser.parseToUnicode("Привет, " + chat.getUserName() + ", рад видеть тебя" + ":blush:");
        }
        return "Здоровались уже";
    }
}
