package org.example.bankcards.controller.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.example.api.InternalCardApi;
import org.example.bankcards.dto.CardDto;
import org.example.bankcards.dto.CreateCardDto;
import org.example.bankcards.entity.Card.CardStatus;
import org.example.bankcards.service.card.CardService;
import org.example.model.CardChangeStatusRequest;
import org.example.model.CardResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InternalCardController implements InternalCardApi {

    private final CardService cardService;

    @Override
    public ResponseEntity<Void> changeStatus(CardChangeStatusRequest cardChangeStatusRequest) {
        try {
            cardService.updateCardStatus(
                    cardChangeStatusRequest.getExternalId(),
                    EnumUtils.getEnum(CardStatus.class, cardChangeStatusRequest.getStatus().name()));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<Void> createCard(org.example.model.CreateCardRequest createCardRequest) {
        try {
            CreateCardDto createCardDto = new CreateCardDto(
                    createCardRequest.getCardNumber(),
                    createCardRequest.getCustomerLogin(),
                    createCardRequest.getExpiryMonth(),
                    createCardRequest.getExpiryYear()
            );
            cardService.createCard(createCardDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<List<CardResponse>> getAllCards(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        try {
            List<CardResponse> cardResponseList = cardService.getCards(pageable).stream()
                    .map(this::cardDtoToResponse)
                    .toList();
            return ResponseEntity.ok(cardResponseList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<List<CardResponse>> getAllCardsByLogin(String login, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        try {
            List<CardResponse> cardResponseList = cardService
                    .getCardsByLogin(login, pageable).stream()
                    .map(this::cardDtoToResponse)
                    .toList();;
            return ResponseEntity.ok(cardResponseList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    public CardResponse cardDtoToResponse(CardDto cardDto) {
        CardResponse response = new CardResponse();
        response.externalId(cardDto.externalId());
        response.cardNumber(cardDto.cardNumber());
        response.expiryMonth(cardDto.expiryMonth());
        response.expiryYear(cardDto.expiryYear());
        response.status(EnumUtils.getEnum(CardResponse.StatusEnum.class, cardDto.status().name()));
        response.balance(cardDto.balance().toString());
        return response;
    }
}
