package com.example.BLPS.auth;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

  private String login;
  String password;
}
