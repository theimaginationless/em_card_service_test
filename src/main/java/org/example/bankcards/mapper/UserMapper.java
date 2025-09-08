package org.example.bankcards.mapper;

import org.example.bankcards.dto.CustomerDto;
import org.example.bankcards.dto.StaffDto;
import org.example.bankcards.entity.User.Customer.Customer;
import org.example.bankcards.entity.User.Staff.Staff;
import org.example.bankcards.util.UserUtil;

public class UserMapper {
    public static CustomerDto customerEntityToDto(Customer customer) {
        return CustomerDto.builder()
                .id(customer.getId())
                .login(customer.getLogin())
                .role(customer.getRole().getRoleType())
                .fullName(UserUtil.getFullName(customer))
                .personalEncryptedJwtSecret(customer.getPeJwtSecret())
                .hashedPassword(customer.getHashedPassword())
                .build();
    }

    public static StaffDto staffEntityToDto(Staff staff) {
        return StaffDto.builder()
                .id(staff.getId())
                .login(staff.getLogin())
                .role(staff.getRole().getRoleType())
                .fullName(UserUtil.getFullName(staff))
                .personalEncryptedJwtSecret(staff.getPeJwtSecret())
                .hashedPassword(staff.getHashedPassword())
                .build();
    }
}
