package org.example.bankcards.service;

import org.example.bankcards.dto.AbstractUserDto;
import org.example.bankcards.exception.UserServiceException;

public interface UserService<T extends AbstractUserDto> {
    T getUserByLogin(String login) throws UserServiceException;
    T getUserById(long id) throws UserServiceException;
    void refreshLastLoginById(long id);
}
