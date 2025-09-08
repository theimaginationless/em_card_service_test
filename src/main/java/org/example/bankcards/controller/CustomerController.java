package org.example.bankcards.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.example.api.CustomerCardApi;
import org.example.bankcards.dto.CardDto;
import org.example.bankcards.exception.AuthCustomerException;
import org.example.bankcards.exception.C2CTransferException;
import org.example.bankcards.service.card.CardService;
import org.example.bankcards.service.card.CardTransferService;
import org.example.bankcards.service.RequestSupportService;
import org.example.bankcards.util.SecurityContextUtil;
import org.example.model.CardResponse;
import org.example.model.TransferRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomerCardApi {

    private final CardService cardService;
    private final CardTransferService cardTransferService;
    private final RequestSupportService requestSupportService;
    

    @Override
    public ResponseEntity<CardResponse> getCard(String id) {
        try {
            var principal = SecurityContextUtil.getGenericPrincipal(SecurityContextHolder.getContext())
                    .orElseThrow(AuthCustomerException::new);
            CardDto cardDto = cardService.getCardByCustomerId(id, principal.getId());
            return ResponseEntity.ok(cardDtoToResponse(cardDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<String> getCardBalance(String id) {
        try {
            var principal = SecurityContextUtil.getGenericPrincipal(SecurityContextHolder.getContext())
                    .orElseThrow(AuthCustomerException::new);
            BigDecimal balance = cardService.getCardBalance(id, principal.getId());
            return ResponseEntity.ok(balance.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<List<CardResponse>> getCards(Integer page, Integer size) {
        return CustomerCardApi.super.getCards(page, size);
    }

    @Override
    public ResponseEntity<Void> requestBlockCard(String id) {
        try {
            var principal = SecurityContextUtil.getGenericPrincipal(SecurityContextHolder.getContext())
                    .orElseThrow(AuthCustomerException::new);
            requestSupportService.requestBlockCard(id, principal.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<Void> transferAmount(TransferRequest transferRequest) {
        try {
            var principal = SecurityContextUtil.getGenericPrincipal(SecurityContextHolder.getContext())
                    .orElseThrow(AuthCustomerException::new);
            cardTransferService.transferAmount(
                    transferRequest.getFromId(),
                    transferRequest.getToId(),
                    new BigDecimal(transferRequest.getAmount()),
                    principal.getId());
            return ResponseEntity.ok().build();
        } catch (C2CTransferException | AuthCustomerException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<?> getCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        try {
            var principal = SecurityContextUtil.getGenericPrincipal(SecurityContextHolder.getContext())
                    .orElseThrow(AuthCustomerException::new);
            List<CardResponse> cardDtoList = cardService.getCardsByCustomerId(pageable, principal.getId()).stream()
                    .map(this::cardDtoToResponse)
                    .toList();
            return ResponseEntity.ok(cardDtoList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    public CardResponse cardDtoToResponse(CardDto cardDto) {
        CardResponse response = new CardResponse();
        response.externalId(cardDto.externalId());
        response.cardNumber(cardDto.cardNumber());
        response.owner(cardDto.owner());
        response.expiryMonth(cardDto.expiryMonth());
        response.expiryYear(cardDto.expiryYear());
        response.status(EnumUtils.getEnum(CardResponse.StatusEnum.class, cardDto.status().name()));
        response.balance(cardDto.balance().toString());
        return response;
    }
}
