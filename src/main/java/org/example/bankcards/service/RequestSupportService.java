package org.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.entity.Card.CardStatus;
import org.example.bankcards.entity.ChangeCardStatusRequest;
import org.example.bankcards.entity.User.Customer.Customer;
import org.example.bankcards.exception.RequestSupportException;
import org.example.bankcards.repository.ChangeCardStatusRequestRepository;
import org.example.bankcards.service.card.CardService;
import org.example.bankcards.service.customer.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestSupportService {

    private final CustomerService customerService;
    private final ChangeCardStatusRequestRepository changeCardStatusRequestRepository;
    private final CardService cardService;

    @Transactional(rollbackFor = RequestSupportException.class)
    public void requestBlockCard(String externalCardId, long customerId) throws RequestSupportException {
        try {
            requestChangeCardStatus(externalCardId, customerId, CardStatus.BLOCKED);
        } catch (RequestSupportException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = RequestSupportException.class)
    public void requestChangeCardStatus(String externalCardId,
                                        long customerId,
                                        CardStatus newStatus)
            throws RequestSupportException {
        try {
            Customer customer = customerService.getCustomerById(customerId);
            Card card = cardService.getCustomerCardForUpdate(externalCardId, customerId);
            validateCardStatus(card.getCardStatus(), newStatus);
            var request = ChangeCardStatusRequest.builder()
                    .card(card)
                    .createdByCustomer(customer)
                    .newCardStatus(newStatus)
                    .requestStatus(ChangeCardStatusRequest.RequestStatus.NEW)
                    .requestId(UUID.randomUUID().toString().replace("-", ""))
                    .build();
            save(request);
        } catch (Exception e) {
            log.error("Create change card status request failed on ecd: {}; cid: {}; newStatus: {}",
                    externalCardId, customerId, newStatus);
            throw new RequestSupportException(e.getMessage());
        }
    }

    public void save(ChangeCardStatusRequest request) {
        changeCardStatusRequestRepository.save(request);
    }

    private void validateCardStatus(CardStatus cardStatus, CardStatus newStatus)
            throws RequestSupportException {
        switch (cardStatus) {
            case EXPIRED -> throw new RequestSupportException("Card expired");
            case ACTIVE -> { }
        }

        if (cardStatus == newStatus) {
            throw new RequestSupportException("Card has the same status: " + newStatus);
        }
    }
}
