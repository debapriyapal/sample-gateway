package com.sample.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.sample.gateway.IntegrationTest;
import com.sample.gateway.domain.User;
import com.sample.gateway.repository.EntityManager;
import com.sample.gateway.repository.UserRepository;
import com.sample.gateway.repository.search.UserSearchRepository;
import com.sample.gateway.security.AuthoritiesConstants;
import com.sample.gateway.service.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link PublicUserResource} REST controller.
 */
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_TIMEOUT)
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class PublicUserResourceIT {

    private static final String DEFAULT_LOGIN = "johndoe";

    @Autowired
    private UserRepository userRepository;

    /**
     * This repository is mocked in the com.sample.gateway.repository.search test package.
     *
     * @see com.sample.gateway.repository.search.UserSearchRepositoryMockConfiguration
     */
    @Autowired
    private UserSearchRepository mockUserSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private User user;

    @BeforeEach
    public void initTest() {
        user = UserResourceIT.initTestUser(userRepository, em);
    }

    @Test
    void getAllPublicUsers() {
        // Initialize the database
        userRepository.save(user).block();

        // Get all the users
        UserDTO foundUser = webTestClient
            .get()
            .uri("/api/users?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .returnResult(UserDTO.class)
            .getResponseBody()
            .blockFirst();

        assertThat(foundUser.getLogin()).isEqualTo(DEFAULT_LOGIN);
    }

    @Test
    void getAllUsersSortedByParameters() throws Exception {
        // Initialize the database
        userRepository.save(user).block();

        webTestClient
            .get()
            .uri("/api/users?sort=resetKey,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isBadRequest();
        webTestClient
            .get()
            .uri("/api/users?sort=password,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isBadRequest();
        webTestClient
            .get()
            .uri("/api/users?sort=resetKey,desc&sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isBadRequest();
        webTestClient.get().uri("/api/users?sort=id,desc").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk();
    }
}
