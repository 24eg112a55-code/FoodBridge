package com.communityfoodsharing.security;

import com.communityfoodsharing.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public CustomUserDetailsService(UserRepository users) { this.users = users; }
    @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return users.findByEmailIgnoreCase(email).map(CustomUserDetails::new)
            .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    }
}