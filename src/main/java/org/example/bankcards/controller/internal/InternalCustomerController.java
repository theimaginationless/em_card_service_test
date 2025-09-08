package org.example.bankcards.controller.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.api.InternalCustomerApi;
import org.example.bankcards.exception.RegisterCustomerException;
import org.example.bankcards.service.InternalCustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InternalCustomerController implements InternalCustomerApi {

    private final InternalCustomerService internalCustomerService;

    @Override
    public ResponseEntity<Void> registerStaff(org.example.model.CustomerRegisterRequest customerRegisterRequest) {
        try {
            internalCustomerService.registerStaff(customerRegisterRequest.getLogin(),
                    customerRegisterRequest.getPassword());
            return ResponseEntity.ok().build();
        } catch (RegisterCustomerException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<Void> registerCustomer(org.example.model.CustomerRegisterRequest customerRegisterRequest) {
        try {
            internalCustomerService.registerCustomer(customerRegisterRequest.getLogin(),
                    customerRegisterRequest.getPassword());
            return ResponseEntity.ok().build();
        } catch (RegisterCustomerException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST).build();
        }
    }
}