package com.example.BLPS.security;

import com.example.BLPS.Entities.User;
import com.example.BLPS.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JaasAuthorityGranter implements AuthorityGranter {

    private final UserRepository userRepository;

    @Override
    public Set<String> grant(Principal principal) {
        User user = userRepository
                .findByLogin(principal.getName())
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        return user.getRole().getPrivileges().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }
}