package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.constants.HeaderConstants.USER_ID;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        // Given
        Long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        ItemRequestDto responseDto = new ItemRequestDto();
        responseDto.setId(1L);
        responseDto.setDescription("Need a drill");
        responseDto.setRequesterId(userId);
        responseDto.setCreated(LocalDateTime.now());

        when(itemRequestService.createRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/requests")
                        .header(USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.requesterId").value(userId));
    }

    @Test
    void getUserRequests_shouldReturnListOfRequests() throws Exception {
        // Given
        Long userId = 1L;
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Need a hammer");
        responseDto.setRequesterId(userId);

        when(itemRequestService.getUserRequests(userId))
                .thenReturn(Collections.singletonList(responseDto));

        // When & Then
        mockMvc.perform(get("/requests")
                        .header(USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Need a hammer"))
                .andExpect(jsonPath("$[0].requesterId").value(userId));
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() throws Exception {
        // Given
        Long userId = 1L;
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(2L);
        responseDto.setDescription("Need a saw");
        responseDto.setRequesterId(2L);

        when(itemRequestService.getAllRequests(userId))
                .thenReturn(Collections.singletonList(responseDto));

        // When & Then
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].description").value("Need a saw"))
                .andExpect(jsonPath("$[0].requesterId").value(2));
    }

    @Test
    void getRequest_shouldReturnRequestById() throws Exception {
        // Given
        Long userId = 1L;
        Long requestId = 1L;
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(requestId);
        responseDto.setDescription("Need a screwdriver");
        responseDto.setRequesterId(userId);

        when(itemRequestService.getRequest(requestId, userId))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Need a screwdriver"))
                .andExpect(jsonPath("$.requesterId").value(userId));
    }

    @Test
    void getRequest_shouldReturn404WhenNotFound() throws Exception {
        // Given
        Long userId = 1L;
        Long requestId = 99L;

        when(itemRequestService.getRequest(requestId, userId))
                .thenThrow(new NotFoundException("Request not found"));

        // When & Then
        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID, userId))
                .andExpect(status().isNotFound());
    }
}