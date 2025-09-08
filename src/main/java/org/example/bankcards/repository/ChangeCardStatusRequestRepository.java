package org.example.bankcards.repository;

import jakarta.persistence.LockModeType;
import org.example.bankcards.entity.ChangeCardStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChangeCardStatusRequestRepository extends CrudRepository<ChangeCardStatusRequest, Long> {
    @EntityGraph(attributePaths = {"card", "createdByCustomer"})
    Page<ChangeCardStatusRequest> findChangeCardStatusRequestsByRequestStatus(ChangeCardStatusRequest.RequestStatus requestStatus, Pageable pageable);

    @Modifying
    @Query("UPDATE ChangeCardStatusRequest r SET r.requestStatus = :status WHERE r.requestId = :requestId")
    void updateRequestStatus(@Param("requestId") String requestId,
                             @Param("status") ChangeCardStatusRequest.RequestStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT ccsr FROM ChangeCardStatusRequest ccsr
        JOIN FETCH ccsr.card
            JOIN FETCH ccsr.createdByCustomer
                WHERE ccsr.requestId = :requestId
    """)
    Optional<ChangeCardStatusRequest> getRequestByRequestId(@Param("requestId") String requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r.requestStatus FROM ChangeCardStatusRequest r WHERE r.requestId = :requestId")
    Optional<ChangeCardStatusRequest.RequestStatus> getRequestStatusForUpdate(@Param("requestId") String requestId);
}
