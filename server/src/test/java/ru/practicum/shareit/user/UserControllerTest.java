package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        // Подготовка данных
        UserDto inputDto = new UserDto();
        inputDto.setName("Test User");
        inputDto.setEmail("test@example.com");

        UserDto outputDto = new UserDto();
        outputDto.setId(1L);
        outputDto.setName("Test User");
        outputDto.setEmail("test@example.com");

        when(userService.createUser(any(UserDto.class))).thenReturn(outputDto);

        // Выполнение запроса и проверки
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        // Подготовка данных
        Long userId = 1L;
        UserDto inputDto = new UserDto();
        inputDto.setName("Updated User");
        inputDto.setEmail("updated@example.com");

        UserDto outputDto = new UserDto();
        outputDto.setId(userId);
        outputDto.setName("Updated User");
        outputDto.setEmail("updated@example.com");

        when(userService.updateUser(anyLong(), any(UserDto.class))).thenReturn(outputDto);

        // Выполнение запроса и проверки
        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService, times(1)).updateUser(anyLong(), any(UserDto.class));
    }

    @Test
    void getUser_shouldReturnUser() throws Exception {
        // Подготовка данных
        Long userId = 1L;
        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        when(userService.getUser(userId)).thenReturn(userDto);

        // Выполнение запроса и проверки
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getUser(userId);
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        // Подготовка данных
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        when(userService.getAllUsers()).thenReturn(Collections.singletonList(userDto));

        // Выполнение запроса и проверки
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test User"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        // Подготовка данных
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        // Выполнение запроса и проверки
        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(userId);
    }
}