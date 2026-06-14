package com.gaab.anivault.persistence.crud;

import com.gaab.anivault.domain.enums.Role;
import com.gaab.anivault.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CrudUserEntityIntegrationTest {

    @Autowired
    private CrudUserEntity crudUserEntity;

    private UserEntity createUser(String username, String email) {
        return UserEntity.builder()
                .username(username)
                .email(email)
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void findByUsername_existing_returnsUser() {
        crudUserEntity.save(createUser("testuser", "test@email.com"));

        Optional<UserEntity> found = crudUserEntity.findByUsername("testuser");

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void findByUsername_nonExisting_returnsEmpty() {
        Optional<UserEntity> found = crudUserEntity.findByUsername("nonexistent");

        assertTrue(found.isEmpty());
    }

    @Test
    void findByEmail_existing_returnsUser() {
        crudUserEntity.save(createUser("testuser", "test@email.com"));

        Optional<UserEntity> found = crudUserEntity.findByEmail("test@email.com");

        assertTrue(found.isPresent());
        assertEquals("test@email.com", found.get().getEmail());
    }

    @Test
    void existsByUsername_trueWhenExists() {
        crudUserEntity.save(createUser("testuser", "test@email.com"));

        assertTrue(crudUserEntity.existsByUsername("testuser"));
        assertFalse(crudUserEntity.existsByUsername("other"));
    }

    @Test
    void existsByEmail_trueWhenExists() {
        crudUserEntity.save(createUser("testuser", "test@email.com"));

        assertTrue(crudUserEntity.existsByEmail("test@email.com"));
        assertFalse(crudUserEntity.existsByEmail("other@email.com"));
    }

    @Test
    void uniqueConstraint_duplicateUsername_throwsException() {
        crudUserEntity.save(createUser("testuser", "test1@email.com"));

        assertThrows(DataIntegrityViolationException.class, () -> {
            crudUserEntity.saveAndFlush(createUser("testuser", "test2@email.com"));
        });
    }

    @Test
    void uniqueConstraint_duplicateEmail_throwsException() {
        crudUserEntity.save(createUser("user1", "test@email.com"));

        assertThrows(DataIntegrityViolationException.class, () -> {
            crudUserEntity.saveAndFlush(createUser("user2", "test@email.com"));
        });
    }

    @Test
    void save_setsCreatedAtAutomatically() {
        UserEntity saved = crudUserEntity.save(createUser("testuser", "test@email.com"));

        assertNotNull(saved.getCreatedAt());
    }
}
