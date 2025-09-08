package org.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bankcards.dto.RoleType;
import org.example.bankcards.entity.User.Customer.Customer;
import org.example.bankcards.entity.User.Role;
import org.example.bankcards.entity.User.Staff.Staff;
import org.example.bankcards.exception.CustomerNotFoundException;
import org.example.bankcards.exception.EncryptSecretException;
import org.example.bankcards.exception.RegisterCustomerException;
import org.example.bankcards.exception.RoleNotFoundException;
import org.example.bankcards.repository.CustomerRepository;
import org.example.bankcards.repository.StaffRepository;
import org.example.bankcards.security.service.JwtService;
import org.example.bankcards.util.SecurityUtil;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalCustomerService {

    private final PasswordService passwordService;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final RoleService roleService;
    private final JwtService jwtService;

    public void registerCustomer(String login, String password)
            throws RegisterCustomerException {
        if (customerRepository.findCustomerByLogin(login).isPresent()) {
            log.debug("Customer with login '{}' already registered", login);
            throw new RegisterCustomerException("Customer is already registered");
        }

        String hashedPassword = passwordService.hashPassword(password);
        String personalSecret = SecurityUtil.generateJwtSecret();
        try {
            String customerEncryptSecret = SecurityUtil.encryptSecret(personalSecret,
                    jwtService.getMasterKey());
            Customer customer = Customer.builder()
                    .hashedPassword(hashedPassword)
                    .peJwtSecret(customerEncryptSecret)
                    .login(login)
                    .role(getUserRole())
                    .build();
            customerRepository.save(customer);
        } catch (EncryptSecretException | RoleNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new RegisterCustomerException();
        }
    }

    public void registerStaff(String login, String password)
            throws RegisterCustomerException {
        if (staffRepository.findStaffByLogin(login).isPresent()) {
            log.debug("Staff with login '{}' already registered", login);
            throw new RegisterCustomerException("Staff is already registered");
        }

        String hashedPassword = passwordService.hashPassword(password);
        String personalSecret = SecurityUtil.generateJwtSecret();
        try {
            String personalEncryptSecret = SecurityUtil.encryptSecret(personalSecret,
                    jwtService.getMasterKey());
            Staff staff = Staff.builder()
                    .hashedPassword(hashedPassword)
                    .peJwtSecret(personalEncryptSecret)
                    .login(login)
                    .role(getAdminRole())
                    .build();
            staffRepository.save(staff);
        } catch (EncryptSecretException | RoleNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new RegisterCustomerException();
        }
    }

    public boolean verifyCredentials(String login, String password) {
        try {
            Customer customer = customerRepository.findCustomerByLogin(login)
                    .orElseThrow(CustomerNotFoundException::new);
            return StringUtils.equals(customer.getLogin(), login)
                    && passwordService.verifyPassword(password, customer.getHashedPassword());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    private Role getUserRole() throws RoleNotFoundException {
        return roleService.getRoleByType(RoleType.USER);
    }

    private Role getAdminRole() throws RoleNotFoundException {
        return roleService.getRoleByType(RoleType.ADMIN);
    }
}
