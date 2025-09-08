package org.example.bankcards.service.card;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.dto.CardDto;
import org.example.bankcards.dto.CreateCardDto;
import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.entity.Card.CardStatus;
import org.example.bankcards.entity.User.Customer.Customer;
import org.example.bankcards.exception.AuthCustomerException;
import org.example.bankcards.exception.CardNotFoundException;
import org.example.bankcards.exception.CreateCardException;
import org.example.bankcards.mapper.CardMapper;
import org.example.bankcards.repository.CardRepository;
import org.example.bankcards.service.customer.CustomerService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.example.bankcards.service.card.CardCacheService.CARD_BALANCE_CACHE_KEY;
import static org.example.bankcards.service.card.CardCacheService.CARD_CACHE_BY_EXTERNAL_CARD_ID_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardCacheService cardCacheService;
    private final CustomerService customerService;

    @Transactional(rollbackFor = CreateCardException.class)
    public void createCard(@Valid CreateCardDto createCardDto) throws CreateCardException {
        try {
            Customer customer = customerService.getCustomerByLoginForShare(createCardDto.customerLogin());
            Card cardEntity = Card.builder()
                    .cardNumber(createCardDto.cardNumber())
                    .cardStatus(CardStatus.ACTIVE)
                    .externalCardId(UUID.randomUUID().toString())
                    .customer(customer)
                    .expiryMonth(createCardDto.expiryMonth())
                    .expiryYear(createCardDto.expiryYear())
                    .build();
            cardRepository.save(cardEntity);
        } catch (Exception e) {
            throw new CreateCardException(e.getMessage());
        }
    }

    public List<CardDto> getCardsByLogin(String login, Pageable pageable) {
        Page<Card> cardsPage = cardRepository.findAllByCustomer_Login(login, pageable);
        return cardsPage.get()
                .map(CardMapper::cardEntityToCardDto)
                .toList();
    }

    public List<CardDto> getCards(Pageable pageable) {
        Page<Card> cardsPage = cardRepository.findAll(pageable);
        return cardsPage.get()
                .map(CardMapper::cardEntityToCardDto)
                .toList();
    }

    public List<CardDto> getCardsByCustomerId(Pageable pageable, long customerId) {
        Page<Card> cardsPage = cardRepository.findAllByCustomer_Id(customerId, pageable);
        return cardsPage.get()
                .map(CardMapper::cardEntityToCardDto)
                .toList();
    }

    @Cacheable(value = CARD_CACHE_BY_EXTERNAL_CARD_ID_KEY, key = "#externalCardId")
    public CardDto getCardByCustomerId(String externalCardId, long customerId) throws CardNotFoundException, AuthCustomerException {
        return cardRepository.findByExternalCardIdAndCustomer_Id(externalCardId, customerId)
                .map(CardMapper::cardEntityToCardDto)
                .orElseThrow(CardNotFoundException::new);
    }

    @Cacheable(value = CARD_BALANCE_CACHE_KEY, key = "#externalCardId")
    public BigDecimal getCardBalance(String externalCardId, long customerId)
            throws CardNotFoundException {
        return cardRepository.getCardBalance(externalCardId, customerId)
                .orElseThrow(CardNotFoundException::new);
    }

    public Card getCustomerCardForUpdate(String externalCardId,
                                               long customerId) throws CardNotFoundException {
        return cardRepository.getCustomerCardForUpdate(externalCardId, customerId)
                .orElseThrow(CardNotFoundException::new);
    }

    @Transactional
    public void updateCardStatus(long id,
                                 String externalCardId,
                                 long customerId,
                                 CardStatus newStatus) throws CardNotFoundException {
        log.info("Trying to update card status cardId: {}; eci: {}; newStatus: {}",
                id, externalCardId, newStatus);
        Card card = cardRepository.getCustomerCardByIdForUpdate(id, customerId)
                .orElseThrow(CardNotFoundException::new);
        card.setCardStatus(newStatus);
        cardRepository.save(card);
        cardCacheService.evictCard(id, externalCardId);
    }

    @Transactional
    public void updateCardStatus(String externalCardId,
                                 CardStatus newStatus) throws CardNotFoundException {
        log.info("Trying to update card status eci: {}; newStatus: {}",
                externalCardId, newStatus);
        Card card = cardRepository.getCardForUpdate(externalCardId)
                .orElseThrow(CardNotFoundException::new);
        card.setCardStatus(newStatus);
        cardRepository.save(card);
        cardCacheService.evictCard(card.getId(), externalCardId);
    }
}
