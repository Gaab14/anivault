package com.gaab.anivault.domain.service;

import com.gaab.anivault.persistence.crud.CrudUserEntity;
import com.gaab.anivault.persistence.entity.UserEntity;
import com.gaab.anivault.web.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final CrudUserEntity crudUserEntity;

    public UserDetailsServiceImpl(CrudUserEntity crudUserEntity) {
        this.crudUserEntity = crudUserEntity;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = crudUserEntity.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );
    }
}
