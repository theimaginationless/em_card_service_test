package org.example.bankcards.mapper;

import org.example.bankcards.dto.ChangeCardStatusRequestDto;
import org.example.bankcards.entity.ChangeCardStatusRequest;
import org.example.bankcards.util.CardUtil;
import org.example.bankcards.util.UserUtil;

public class ChangeCardStatusRequestMapper {
    public static ChangeCardStatusRequestDto changeCardStatusRequestEntityToDto(ChangeCardStatusRequest entity) {
        return new ChangeCardStatusRequestDto(
                entity.getRequestId(),
                entity.getCard().getExternalCardId(),
                CardUtil.maskCardNumber(entity.getCard().getCardNumber()),
                UserUtil.getFullName(entity.getCreatedByCustomer()),
                entity.getCard().getExpiryMonth(),
                entity.getCard().getExpiryYear(),
                entity.getCard().getCardStatus().name(),
                entity.getNewCardStatus().name()
        );
    }
}
