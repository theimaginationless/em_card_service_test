package org.example.bankcards.config;

import lombok.RequiredArgsConstructor;
import org.example.bankcards.service.customer.CustomerDaService;
import org.example.bankcards.service.customer.CustomerService;
import org.example.bankcards.service.staff.StaffDaService;
import org.example.bankcards.service.staff.StaffService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final CustomerDaService customerDaService;
    private final StaffDaService staffDaService;

    @Bean(name = CustomerService.SERVICE_NAME)
    public CustomerService customerService() {
        return new CustomerService(customerDaService);
    }

    @Bean(name = StaffService.SERVICE_NAME)
    public StaffService staffService() {
        return new StaffService(staffDaService);
    }
}
