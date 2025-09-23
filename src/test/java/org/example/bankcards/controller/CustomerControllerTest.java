package org.example.bankcards.controller;

import org.example.bankcards.dto.CardDto;
import org.example.bankcards.entity.Card.CardStatus;
import org.example.bankcards.exception.C2CTransferException;
import org.example.bankcards.security.principal.GenericPrincipal;
import org.example.bankcards.service.RequestSupportService;
import org.example.bankcards.service.card.CardService;
import org.example.bankcards.service.card.CardTransferFgService;
import org.example.bankcards.service.card.CardTransferService;
import org.example.bankcards.util.SecurityContextUtil;
import org.example.model.CardResponse;
import org.example.model.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class CustomerControllerTest {

    private CardService cardService;
    private CardTransferService cardTransferService;
    private CardTransferFgService cardTransferFgService;
    private RequestSupportService requestSupportService;
    private CustomerController controller;

    private GenericPrincipal principal;

    @BeforeEach
    void setup() {
        cardService = mock(CardService.class);
        cardTransferService = mock(CardTransferService.class);
        cardTransferFgService = mock(CardTransferFgService.class);
        requestSupportService = mock(RequestSupportService.class);
        controller = new CustomerController(cardService, cardTransferFgService, requestSupportService);

        principal = GenericPrincipal.builder()
                .id(1L)
                .login("user")
                .build();
    }

    @Test
    void getCard_Success() throws Exception {
        CardDto cardDto = new CardDto(1, "ext123", "1234 5678 9012 3456", "Owner", 12, 2025, CardStatus.ACTIVE, BigDecimal.valueOf(100));
        when(cardService.getCardByCustomerId("ext123", 1L)).thenReturn(cardDto);

        try (MockedStatic<SecurityContextUtil> securityMock = Mockito.mockStatic(SecurityContextUtil.class)) {
            securityMock.when(() -> SecurityContextUtil.getGenericPrincipal(any())).thenReturn(Optional.of(principal));

            ResponseEntity<CardResponse> response = controller.getCard("ext123");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("ext123", response.getBody().getExternalId());
            assertEquals("1234 5678 9012 3456", response.getBody().getCardNumber());
        }
    }

    @Test
    void getCard_AuthException_BadRequest() {
        try (MockedStatic<SecurityContextUtil> securityMock = Mockito.mockStatic(SecurityContextUtil.class)) {
            securityMock.when(() -> SecurityContextUtil.getGenericPrincipal(any())).thenReturn(Optional.empty());

            ResponseEntity<CardResponse> response = controller.getCard("ext123");
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Test
    void getCardBalance_Success() throws Exception {
        when(cardService.getCardBalance("ext123", 1L)).thenReturn(BigDecimal.valueOf(250));

        try (MockedStatic<SecurityContextUtil> securityMock = Mockito.mockStatic(SecurityContextUtil.class)) {
            securityMock.when(() -> SecurityContextUtil.getGenericPrincipal(any())).thenReturn(Optional.of(principal));

            ResponseEntity<String> response = controller.getCardBalance("ext123");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("250", response.getBody());
        }
    }

    @Test
    void requestBlockCard_Success() throws Exception {
        doNothing().when(requestSupportService).requestBlockCard("ext123", 1L);

        try (MockedStatic<SecurityContextUtil> securityMock = Mockito.mockStatic(SecurityContextUtil.class)) {
            securityMock.when(() -> SecurityContextUtil.getGenericPrincipal(any())).thenReturn(Optional.of(principal));

            ResponseEntity<Void> response = controller.requestBlockCard("ext123");
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        verify(requestSupportService, times(1)).requestBlockCard("ext123", 1L);
    }

    @Test
    void transferAmount_Success() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromId("from123");
        request.setToId("to456");
        request.setAmount("500");

        doNothing().when(cardTransferFgService).transferAmount(anyString(), anyString(), any(BigDecimal.class), eq(1L));

        try (MockedStatic<SecurityContextUtil> securityMock = Mockito.mockStatic(SecurityContextUtil.class)) {
            securityMock.when(() -> SecurityContextUtil.getGenericPrincipal(any())).thenReturn(Optional.of(principal));

            ResponseEntity<Void> response = controller.transferAmount(request);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        verify(cardTransferFgService, times(1))
                .transferAmount("from123", "to456", BigDecimal.valueOf(500), 1L);
    }

    @Test
    void transferAmount_C2CTransferException_BadRequest() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromId("from123");
        request.setToId("to456");
        request.setAmount("500");

        doThrow(new C2CTransferException("Error")).when(cardTransferFgService)
                .transferAmount(anyString(), anyString(), any(BigDecimal.class), eq(1L));

        try (MockedStatic<SecurityContextUtil> securityMock = Mockito.mockStatic(SecurityContextUtil.class)) {
            securityMock.when(() -> SecurityContextUtil.getGenericPrincipal(any())).thenReturn(Optional.of(principal));

            ResponseEntity<Void> response = controller.transferAmount(request);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }
}
