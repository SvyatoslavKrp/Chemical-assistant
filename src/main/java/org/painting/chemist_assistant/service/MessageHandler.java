package org.painting.chemist_assistant.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageHandler {
    SendMessage handle(Message message);
    void setNextHandler(MessageHandler nextHandler);

}

