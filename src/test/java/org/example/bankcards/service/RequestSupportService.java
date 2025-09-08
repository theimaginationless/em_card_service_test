package org.example.bankcards.service;

import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.entity.Card.CardStatus;
import org.example.bankcards.entity.ChangeCardStatusRequest;
import org.example.bankcards.entity.User.Customer.Customer;
import org.example.bankcards.exception.RequestSupportException;
import org.example.bankcards.repository.ChangeCardStatusRequestRepository;
import org.example.bankcards.service.card.CardService;
import org.example.bankcards.service.customer.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestSupportServiceTest {

    private CustomerService customerService;
    private CardService cardService;
    private ChangeCardStatusRequestRepository repository;
    private RequestSupportService requestSupportService;

    @BeforeEach
    void setup() {
        customerService = mock(CustomerService.class);
        cardService = mock(CardService.class);
        repository = mock(ChangeCardStatusRequestRepository.class);

        requestSupportService = new RequestSupportService(customerService, repository, cardService);
    }

    @Test
    void requestBlockCard_Success() throws Exception {
        Customer customer = mock(Customer.class);
        Card card = mock(Card.class);
        when(customerService.getCustomerById(1L)).thenReturn(customer);
        when(cardService.getCustomerCardForUpdate("card123", 1L)).thenReturn(card);
        when(card.getCardStatus()).thenReturn(CardStatus.ACTIVE);

        requestSupportService.requestBlockCard("card123", 1L);

        ArgumentCaptor<ChangeCardStatusRequest> captor = ArgumentCaptor.forClass(ChangeCardStatusRequest.class);
        verify(repository).save(captor.capture());

        ChangeCardStatusRequest savedRequest = captor.getValue();
        assertEquals(CardStatus.BLOCKED, savedRequest.getNewCardStatus());
        assertEquals(customer, savedRequest.getCreatedByCustomer());
        assertEquals(card, savedRequest.getCard());
        assertNotNull(savedRequest.getRequestId());
    }

    @Test
    void requestChangeCardStatus_SameStatus_ThrowsException() throws Exception {
        Customer customer = mock(Customer.class);
        Card card = mock(Card.class);
        when(customerService.getCustomerById(1L)).thenReturn(customer);
        when(cardService.getCustomerCardForUpdate("card123", 1L)).thenReturn(card);
        when(card.getCardStatus()).thenReturn(CardStatus.ACTIVE);

        RequestSupportException exception = assertThrows(RequestSupportException.class,
                () -> requestSupportService.requestChangeCardStatus("card123", 1L, CardStatus.ACTIVE));

        assertEquals("Card has the same status: ACTIVE", exception.getMessage());
    }

    @Test
    void requestChangeCardStatus_ExpiredCard_ThrowsException() throws Exception {
        Customer customer = mock(Customer.class);
        Card card = mock(Card.class);
        when(customerService.getCustomerById(1L)).thenReturn(customer);
        when(cardService.getCustomerCardForUpdate("card123", 1L)).thenReturn(card);
        when(card.getCardStatus()).thenReturn(CardStatus.EXPIRED);

        RequestSupportException exception = assertThrows(RequestSupportException.class,
                () -> requestSupportService.requestChangeCardStatus("card123", 1L, CardStatus.BLOCKED));

        assertEquals("Card expired", exception.getMessage());
    }

    @Test
    void requestChangeCardStatus_RepositoryCalled() throws Exception {
        Customer customer = mock(Customer.class);
        Card card = mock(Card.class);
        when(customerService.getCustomerById(1L)).thenReturn(customer);
        when(cardService.getCustomerCardForUpdate("card123", 1L)).thenReturn(card);
        when(card.getCardStatus()).thenReturn(CardStatus.ACTIVE);

        requestSupportService.requestChangeCardStatus("card123", 1L, CardStatus.BLOCKED);

        verify(repository, times(1)).save(any(ChangeCardStatusRequest.class));
    }
}
