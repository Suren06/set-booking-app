package com.setbook.controller;

import com.setbook.dto.AuthenticationRequest;
import com.setbook.dto.SignUpRequest;
import com.setbook.dto.UserDto;
import com.setbook.model.User;
import com.setbook.repository.UserRepository;
import com.setbook.services.auth.AuthService;
import com.setbook.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    private static final String HEADER_STRING = "Authorization";

    private static final String TOKEN_PREFIX = "Bearer ";

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUpUser(@RequestBody SignUpRequest signUpRequest) {
        if (authService.hasUserWithEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>("User already exists", HttpStatus.NOT_ACCEPTABLE);
        }

        UserDto userDto = authService.createUser(signUpRequest);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        log.info("authenticationRequest000::{}", authenticationRequest);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUserName(), authenticationRequest.getPassword()));
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUserName());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());

            log.info("authenticationRequest::{}", authenticationRequest);

            Optional<User> optionalUser = userRepository.findFirstByEmail(userDetails.getUsername());
            if (optionalUser.isPresent()) {
                JSONObject responseJson = new JSONObject();
                responseJson.put("userId", optionalUser.get().getId());
                responseJson.put("role", optionalUser.get().getUserRole());

                // Adding headers directly to the ResponseEntity
                return ResponseEntity.ok()
                        .header("Access-Control-Expose-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin")
                        .header("Access-Control-Allow-Headers", "Authorization, X-PINGOTHER, Origin, Content-Type, Accept, X-Requested-With, Access-Control-Request-Method, Access-Control-Request-Headers")
                        .header(HEADER_STRING, TOKEN_PREFIX + jwt)
                        .body(responseJson.toString());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found.");
            }
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Incorrect username or password.");
        } catch (Exception ex) {
            log.info("ex::{}", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

}
