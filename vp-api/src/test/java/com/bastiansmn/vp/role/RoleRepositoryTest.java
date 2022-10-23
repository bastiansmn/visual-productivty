package com.bastiansmn.vp.role;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void findByName() {
        // Given
        RoleDAO roleUser = RoleDAO.builder()
                .name("ROLE_USER")
                .build();

        RoleDAO roleAdmin = RoleDAO.builder()
                .name("ROLE_ADMIN")
                .build();

        this.underTest.save(roleUser);
        this.underTest.save(roleAdmin);

        // When
        Optional<RoleDAO> userExpected = this.underTest.findByName("ROLE_USER");
        Optional<RoleDAO> adminExpected = this.underTest.findByName("ROLE_ADMIN");

        // Then
        assertThat(userExpected).isNotEmpty();
        assertThat(adminExpected).isNotEmpty();
        assertThat(userExpected.get().getName()).isEqualTo(roleUser.getName());
        assertThat(adminExpected.get().getName()).isEqualTo(roleAdmin.getName());
    }
}