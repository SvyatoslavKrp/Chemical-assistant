package org.painting.chemist_assistant.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.painting.chemist_assistant.domain.Workshop;
import org.painting.chemist_assistant.repository.WorkshopRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class InformationCommandHandler extends BaseHandler {

    private final WorkshopRepository workshopRepository;
    @Override
    public SendMessage handle(Message message) {

        if (StringUtils.equals(message.getText(), "/information")) {
            return getInfoOptions(message);
        } else if (StringUtils.startsWith(message.getText(), "workshop_description_")) {
            return sendUserWorkshopInformation(message);
        } else if (nextHandler != null) {
            return nextHandler.handle(message);
        }
        return null;
    }

    private SendMessage getInfoOptions(Message message) {

        Long chatId = message.getChatId();
        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(String.valueOf(chatId));
        List<Workshop> workshops = workshopRepository.findAll();
        if (workshops.isEmpty()) {
            messageToSend.setText("Нет информации об участках");
            return messageToSend;
        }

        messageToSend.setText("Информацию о каком участке Вы хотите получить?");

        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineRow = new ArrayList<>();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();

        for (Workshop workshop : workshops) {
            InlineKeyboardButton workshopButton = createButton(workshop.getName(), "workshop_description_" + workshop.getId());
            inlineRow.add(workshopButton);
        }
        inlineRows.add(inlineRow);
        markupLine.setKeyboard(inlineRows);
        messageToSend.setReplyMarkup(markupLine);
        return messageToSend;
    }

    private SendMessage sendUserWorkshopInformation(Message message) {
        String workshopId = message.getText().replace("workshop_description_", "");
        Long chatId = message.getChat().getId();
        Optional<Workshop> optionalWorkshop = workshopRepository.findById(Long.valueOf(workshopId));
        if (optionalWorkshop.isPresent()) {
            SendMessage messageToSend = new SendMessage();
            messageToSend.setChatId(String.valueOf(chatId));
            messageToSend.setText(optionalWorkshop.get().getDescription());
            log.info("The user (id = " + chatId + " has asked information about workshop (id = " + workshopId + ")");
            return messageToSend;
        }
        return null;
    }

}
