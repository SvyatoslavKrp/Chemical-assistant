package org.painting.chemist_assistant.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@RequiredArgsConstructor
@Service
public class InstructionCommandHandler extends BaseHandler{
    @Override
    public List<SendMessage> handle(Message message) {
        return null;
    }
}
