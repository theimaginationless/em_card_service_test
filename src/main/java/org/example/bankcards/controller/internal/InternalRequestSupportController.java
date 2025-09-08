package org.example.bankcards.controller.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.example.api.InternalRequestSupportApi;
import org.example.bankcards.dto.ChangeCardStatusRequestDto;
import org.example.bankcards.entity.ChangeCardStatusRequest;
import org.example.bankcards.exception.RequestSupportException;
import org.example.bankcards.exception.StaffNotFoundException;
import org.example.bankcards.security.principal.GenericPrincipal;
import org.example.bankcards.service.card.InternalRequestSupportService;
import org.example.bankcards.util.SecurityContextUtil;
import org.example.model.RequestResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InternalRequestSupportController implements InternalRequestSupportApi {

    private final InternalRequestSupportService internalRequestSupportService;

    @Override
    public ResponseEntity<Void> executeRequest(String requestId) {
        try {
            internalRequestSupportService.executeSupportRequest(requestId);
            return ResponseEntity.ok().build();
        } catch (RequestSupportException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<List<RequestResponse>> getNewRequests(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        try {
            List<RequestResponse> requestDtoList = internalRequestSupportService
                    .getRequestsByStatus(pageable, ChangeCardStatusRequest.RequestStatus.NEW)
                    .stream()
                    .map(this::requestDtoToResponse)
                    .toList();
            return ResponseEntity.ok(requestDtoList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<Void> takeRequest(String requestId) {
        try {
            String login = SecurityContextUtil.getGenericPrincipal(SecurityContextHolder.getContext())
                    .map(GenericPrincipal::getLogin)
                    .orElseThrow(StaffNotFoundException::new);
            internalRequestSupportService.takeRequest(requestId, login);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private RequestResponse requestDtoToResponse(ChangeCardStatusRequestDto dto) {
        RequestResponse response = new RequestResponse();
        response.setRequestId(dto.requestId());
        response.setExternalId(dto.externalId());
        response.setCardNumber(dto.cardNumber());
        response.setOwner(dto.owner());
        response.expiryMonth(dto.expiryMonth());
        response.expiryYear(dto.expiryYear());
        response.status(EnumUtils.getEnum(RequestResponse.StatusEnum.class, dto.status()));
        response.newStatus(EnumUtils.getEnum(RequestResponse.NewStatusEnum.class, dto.newStatus()));
        return response;
    }
}
