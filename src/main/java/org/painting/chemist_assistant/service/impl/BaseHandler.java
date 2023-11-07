package org.painting.chemist_assistant.service.impl;

import org.painting.chemist_assistant.service.MessageHandler;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public abstract class BaseHandler implements MessageHandler {

    MessageHandler nextHandler;
    static final Logger log = org.slf4j.LoggerFactory.getLogger(BaseHandler.class);

    @Override
    public abstract List<SendMessage> handle(Message message);

    @Override
    public void setNextHandler(MessageHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
    InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
