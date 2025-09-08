package org.example.bankcards.service.customer;

import lombok.RequiredArgsConstructor;
import org.example.bankcards.entity.User.Customer.Customer;
import org.example.bankcards.exception.CustomerNotFoundException;
import org.example.bankcards.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CustomerDaService {

    private final CustomerRepository customerRepository;

    public Customer getCustomerById(long id) throws CustomerNotFoundException {
        return customerRepository.findCustomerById(id)
                .orElseThrow(CustomerNotFoundException::new);
    }

    public Customer getCustomerByLogin(String login) throws CustomerNotFoundException {
        return customerRepository.findCustomerByLogin(login)
                .orElseThrow(CustomerNotFoundException::new);
    }

    public Customer getCustomerByLoginForShare(String login) throws CustomerNotFoundException {
        return customerRepository.getCustomerByLoginForShare(login)
                .orElseThrow(CustomerNotFoundException::new);
    }

    public void refreshLastLoginById(long id) {
        customerRepository.updateLastLogin(id, Instant.now());
    }
}
