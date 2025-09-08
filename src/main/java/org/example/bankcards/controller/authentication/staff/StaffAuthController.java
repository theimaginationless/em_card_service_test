package org.example.bankcards.controller.authentication.staff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.api.StaffAuthApi;
import org.example.bankcards.exception.AuthCustomerException;
import org.example.bankcards.security.service.AuthService;
import org.example.model.LoginRequest;
import org.example.model.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StaffAuthController implements StaffAuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<LoginResponse> loginStaff(LoginRequest loginRequest) {
        try {
            String token = authService.staffAuthenticate(loginRequest.getLogin(),
                    loginRequest.getPassword());
            return ResponseEntity.ok(new LoginResponse().token(token));
        } catch (AuthCustomerException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}