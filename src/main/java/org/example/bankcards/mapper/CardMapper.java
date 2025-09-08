package org.example.bankcards.mapper;

import org.example.bankcards.dto.CardDto;
import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.util.CardUtil;
import org.example.bankcards.util.UserUtil;

public class CardMapper {
    public static CardDto cardEntityToCardDto(Card card) {
        return new CardDto(
                card.getId(),
                card.getExternalCardId(),
                CardUtil.maskCardNumber(card.getCardNumber()),
                UserUtil.getFullName(card.getCustomer()),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getCardStatus(),
                card.getBalance()
        );
    }
}
