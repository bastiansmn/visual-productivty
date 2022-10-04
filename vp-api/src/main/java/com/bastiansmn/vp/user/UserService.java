package com.bastiansmn.vp.user;

import com.bastiansmn.vp.exception.FunctionalException;
import com.bastiansmn.vp.user.dto.UserCreationDTO;

import java.util.List;

public interface UserService {

    UserDAO create(UserCreationDTO user) throws FunctionalException;

    UserDAO fetchByID(Long id) throws FunctionalException;

    void delete(Long id) throws FunctionalException;

    List<UserDAO> fetchAll();

    boolean emailExists(String email);

    boolean isEnabled(String email) throws FunctionalException;

    boolean isNotLocked(String email) throws FunctionalException;

}
