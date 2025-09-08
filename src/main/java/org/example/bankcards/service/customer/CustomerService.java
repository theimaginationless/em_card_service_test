package org.example.bankcards.service.customer;

import lombok.RequiredArgsConstructor;
import org.example.bankcards.dto.CustomerDto;
import org.example.bankcards.entity.User.Customer.Customer;
import org.example.bankcards.exception.CustomerNotFoundException;
import org.example.bankcards.mapper.UserMapper;
import org.example.bankcards.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService implements UserService<CustomerDto> {

    public static final String SERVICE_NAME = "CUSTOMER_SERVICE";
    private final CustomerDaService customerDaService;

    public Customer getCustomerById(long id) throws CustomerNotFoundException {
        return customerDaService.getCustomerById(id);
    }

    public Customer getCustomerByLogin(String login) throws CustomerNotFoundException {
        return customerDaService.getCustomerByLogin(login);
    }

    public Customer getCustomerByLoginForShare(String login) throws CustomerNotFoundException {
        return customerDaService.getCustomerByLoginForShare(login);
    }

    @Override
    public void refreshLastLoginById(long id) {
        customerDaService.refreshLastLoginById(id);
    }

    @Override
    public CustomerDto getUserByLogin(String login) throws CustomerNotFoundException {
        return UserMapper.customerEntityToDto(customerDaService.getCustomerByLogin(login));
    }

    @Override
    public CustomerDto getUserById(long id) throws CustomerNotFoundException {
        return UserMapper.customerEntityToDto(customerDaService.getCustomerById(id));
    }
}
