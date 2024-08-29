package com.setbook.services.auth;

import com.setbook.dto.SignUpRequest;
import com.setbook.dto.UserDto;

public interface AuthService {

    UserDto createUser(SignUpRequest signUpRequest);

    Boolean hasUserWithEmail(String email);
}
