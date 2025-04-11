package com.example.BLPS.auth;

import com.example.BLPS.Entities.Role;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
  private String login;
  private String password;
  private Role role;
}
