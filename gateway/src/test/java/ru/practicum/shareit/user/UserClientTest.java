package ru.practicum.shareit.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.user.dto.UserDto;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(UserClient.class)
@ActiveProfiles("test")
class UserClientTest {

    @Autowired
    private UserClient userClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    void contextLoads() {
        assertNotNull(userClient);
        assertNotNull(mockServer);
    }

    @Test
    void createUser_shouldSendPostRequest() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        String expectedResponse = "{\"id\":1,\"name\":\"Test User\",\"email\":\"test@example.com\"}";

        // When
        mockServer.expect(requestTo("http://testserver/users"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = userClient.createUser(userDto);

        // Then
        mockServer.verify();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateUser_shouldSendPatchRequest() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setName("Updated User");
        userDto.setEmail("updated@example.com");

        String expectedResponse = "{\"id\":1,\"name\":\"Updated User\",\"email\":\"updated@example.com\"}";

        // When
        mockServer.expect(requestTo("http://testserver/users/1"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = userClient.updateUser(1L, userDto);

        // Then
        mockServer.verify();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getUser_shouldSendGetRequest() throws Exception {
        // Given
        String expectedResponse = "{\"id\":1,\"name\":\"Test User\",\"email\":\"test@example.com\"}";

        // When
        mockServer.expect(requestTo("http://testserver/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = userClient.getUser(1L);

        // Then
        mockServer.verify();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void deleteUser_shouldSendDeleteRequest() throws Exception {
        // When
        mockServer.expect(requestTo("http://testserver/users/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        ResponseEntity<Object> response = userClient.deleteUser(1L);

        // Then
        mockServer.verify();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}