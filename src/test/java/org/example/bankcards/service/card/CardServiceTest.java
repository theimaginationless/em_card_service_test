package org.example.bankcards.service.card;

import org.example.bankcards.dto.CardDto;
import org.example.bankcards.dto.CreateCardDto;
import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.entity.Card.CardStatus;
import org.example.bankcards.entity.User.Customer.Customer;
import org.example.bankcards.exception.CardNotFoundException;
import org.example.bankcards.exception.CreateCardException;
import org.example.bankcards.exception.CustomerNotFoundException;
import org.example.bankcards.repository.CardRepository;
import org.example.bankcards.service.customer.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    private CardRepository cardRepository;
    private CardCacheService cardCacheService;
    private CustomerService customerService;
    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        cardCacheService = mock(CardCacheService.class);
        customerService = mock(CustomerService.class);
        cardService = new CardService(cardRepository, cardCacheService, customerService);
    }

    @Test
    void testCreateCard_Success() throws CreateCardException, CustomerNotFoundException {
        CreateCardDto dto = new CreateCardDto("1234 1234 1234 1234", "john_doe", 12, 2027);
        Customer customer = new Customer();
        when(customerService.getCustomerByLoginForShare(dto.customerLogin())).thenReturn(customer);

        cardService.createCard(dto);

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void testCreateCard_Exception() throws Exception {
        CreateCardDto dto = new CreateCardDto("1234 1234 1234 1234", "john_doe", 12, 2027);
        when(customerService.getCustomerByLoginForShare(dto.customerLogin()))
                .thenThrow(new RuntimeException("DB error"));

        CreateCardException exception = assertThrows(CreateCardException.class,
                () -> cardService.createCard(dto));
        assertEquals("DB error", exception.getMessage());
    }

    @Test
    void testGetCardsByCustomerId_ReturnsDtos() {
        Card card = new Card();
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setMiddleName("Hz");
        card.setCustomer(customer);
        card.setExternalCardId("eci-1234");
        card.setCardNumber("1234 1234 1234 1234");
        card.setExpiryMonth(12);
        card.setExpiryYear(2027);
        card.setCardStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(100));

        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findAllByCustomer_Id(1L, Pageable.unpaged())).thenReturn(page);

        List<CardDto> result = cardService.getCardsByCustomerId(Pageable.unpaged(), 1L);

        assertEquals(1, result.size());
        CardDto dto = result.getFirst();
        assertEquals("eci-1234", dto.externalId());
        assertEquals("**** **** **** 1234", dto.cardNumber());
        assertEquals("John Doe Hz", dto.owner());
        assertEquals(12, dto.expiryMonth());
        assertEquals(2027, dto.expiryYear());
        assertEquals("ACTIVE", dto.status().name());
        assertEquals(BigDecimal.valueOf(100), dto.balance());
    }

    @Test
    void testGetCardByCustomerId_Success() throws Exception {
        Card card = new Card();
        Customer customer = new Customer();
        customer.setFirstName("John");
        card.setCustomer(customer);
        card.setExternalCardId("eci-1234");
        card.setCardNumber("1234 1234 1234 1234");
        card.setExpiryMonth(12);
        card.setExpiryYear(2027);
        card.setCardStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(100));

        when(cardRepository.findByExternalCardIdAndCustomer_Id("eci-1234", 1L))
                .thenReturn(Optional.of(card));

        assertNotNull(cardService.getCardByCustomerId("eci-1234", 1L));
    }

    @Test
    void testGetCardByCustomerId_NotFound() {
        when(cardRepository.findByExternalCardIdAndCustomer_Id("eci-1234", 1L))
                .thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.getCardByCustomerId("eci-1234", 1L));
    }

    @Test
    void testGetCardBalance_Success() throws Exception {
        when(cardRepository.getCardBalance("eci-1234", 1L))
                .thenReturn(Optional.of(BigDecimal.valueOf(100)));

        BigDecimal balance = cardService.getCardBalance("eci-1234", 1L);
        assertEquals(BigDecimal.valueOf(100), balance);
    }

    @Test
    void testGetCardBalance_NotFound() {
        when(cardRepository.getCardBalance("eci-1234", 1L))
                .thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.getCardBalance("eci-1234", 1L));
    }
}
