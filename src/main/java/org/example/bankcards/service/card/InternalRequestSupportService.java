package org.example.bankcards.service.card;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.dto.ChangeCardStatusRequestDto;
import org.example.bankcards.entity.Card.CardStatus;
import org.example.bankcards.entity.ChangeCardStatusRequest;
import org.example.bankcards.exception.RequestSupportException;
import org.example.bankcards.mapper.ChangeCardStatusRequestMapper;
import org.example.bankcards.repository.ChangeCardStatusRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.example.bankcards.entity.ChangeCardStatusRequest.RequestStatus.DONE;
import static org.example.bankcards.entity.ChangeCardStatusRequest.RequestStatus.PROCESSING;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalRequestSupportService {

    private final ChangeCardStatusRequestRepository changeCardStatusRequestRepository;
    private final CardService cardService;

    @Transactional(rollbackFor = RequestSupportException.class)
    public void executeSupportRequest(String requestId) throws RequestSupportException {
        log.info("Executing support request rid={}", requestId);
        try {
            var request = changeCardStatusRequestRepository.getRequestByRequestId(requestId)
                   .orElseThrow(RequestSupportException::new);
           validateRequestStatus(request.getRequestStatus(), DONE);
           validateCardStatus(request.getCard().getCardStatus(), request.getNewCardStatus());
           cardService.updateCardStatus(request.getCard().getId(),
                   request.getCard().getExternalCardId(),
                   request.getCreatedByCustomer().getId(),
                   request.getNewCardStatus());
           request.setRequestStatus(ChangeCardStatusRequest.RequestStatus.DONE);
           changeCardStatusRequestRepository.save(request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RequestSupportException(e.getMessage());
        }
    }

    @Transactional
    public List<ChangeCardStatusRequestDto> getRequestsByStatus(Pageable pageable,
                                                                ChangeCardStatusRequest.RequestStatus status) {
        Page<ChangeCardStatusRequest> requests = changeCardStatusRequestRepository
                .findChangeCardStatusRequestsByRequestStatus(status, pageable);
        return requests.get()
                .map(ChangeCardStatusRequestMapper::changeCardStatusRequestEntityToDto)
                .toList();
    }

    @Transactional(rollbackFor = RequestSupportException.class)
    public void takeRequest(String requestId, String byLogin) throws RequestSupportException {
        var requestStatus = changeCardStatusRequestRepository.getRequestStatusForUpdate(requestId)
                .orElseThrow(RequestSupportException::new);
        validateRequestStatus(requestStatus, PROCESSING);
        changeCardStatusRequestRepository.updateRequestStatus(requestId,
                ChangeCardStatusRequest.RequestStatus.PROCESSING);
        log.info("Request {} taken by {}", requestId, byLogin);
    }

    private void validateRequestStatus(ChangeCardStatusRequest.RequestStatus requestStatus,
                                       ChangeCardStatusRequest.RequestStatus newStatus) throws RequestSupportException {
        if (requestStatus.equals(newStatus)) {
            throw new RequestSupportException("Attempt to change to the same status");
        }
        switch (requestStatus) {
            case REJECTED, DONE -> throw new RequestSupportException("Request in status: " + requestStatus);
            case NEW -> {
                if (!PROCESSING.equals(newStatus))
                    throw new RequestSupportException("Attempt to change status without processing");
            }
            case PROCESSING -> { }
            default -> throw new RequestSupportException("Unknown status: " + newStatus);
        }
    }

    private void validateCardStatus(CardStatus cardStatus, CardStatus newCardStatus)
            throws RequestSupportException {
        if (cardStatus == newCardStatus) {
            throw new RequestSupportException("Card already has status: " + cardStatus);
        }
    }
}
