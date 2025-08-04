package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.constants.HeaderConstants.USER_ID;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        // Given
        ItemDto inputDto = new ItemDto();
        inputDto.setName("Test Item");
        ItemDto outputDto = new ItemDto();
        outputDto.setId(1L);
        outputDto.setName("Test Item");

        when(itemService.createItem(anyLong(), any(ItemDto.class))).thenReturn(outputDto);

        // When & Then
        mockMvc.perform(post("/items")
                        .header(USER_ID, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated Name");
        updateDto.setDescription("Updated Description");
        updateDto.setAvailable(false);

        ItemDto expectedDto = new ItemDto();
        expectedDto.setId(itemId);
        expectedDto.setName("Updated Name");
        expectedDto.setDescription("Updated Description");
        expectedDto.setAvailable(false);

        when(itemService.updateItem(eq(userId), eq(itemId), any(ItemUpdateDto.class)))
                .thenReturn(expectedDto);

        // When & Then
        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void updateItem_shouldReturn404WhenItemNotFound() throws Exception {
        // Given
        Long itemId = 99L;
        Long userId = 1L;
        ItemUpdateDto updateDto = new ItemUpdateDto();

        when(itemService.updateItem(eq(userId), eq(itemId), any(ItemUpdateDto.class)))
                .thenThrow(new NotFoundException("Item not found"));

        // When & Then
        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_shouldReturn403WhenNotOwner() throws Exception {
        // Given
        Long itemId = 1L;
        Long userId = 2L; // Не владелец
        ItemUpdateDto updateDto = new ItemUpdateDto();

        when(itemService.updateItem(eq(userId), eq(itemId), any(ItemUpdateDto.class)))
                .thenThrow(new ForbiddenException("Only owner can update item"));

        // When & Then
        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getItem_shouldReturnItem() throws Exception {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        ItemDto expectedDto = new ItemDto();
        expectedDto.setId(itemId);
        expectedDto.setName("Test Item");
        expectedDto.setDescription("Test Description");
        expectedDto.setAvailable(true);

        when(itemService.getItem(eq(itemId), eq(userId)))
                .thenReturn(expectedDto);

        // When & Then
        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void getItem_shouldReturn404WhenItemNotFound() throws Exception {
        // Given
        Long itemId = 99L;
        Long userId = 1L;

        when(itemService.getItem(eq(itemId), eq(userId)))
                .thenThrow(new NotFoundException("Item not found"));

        // When & Then
        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID, userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserItems_shouldReturnListOfItems() throws Exception {
        // Given
        Long userId = 1L;

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");

        when(itemService.getUserItems(eq(userId)))
                .thenReturn(List.of(itemDto));

        // When & Then
        mockMvc.perform(get("/items")
                        .header(USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void getUserItems_shouldReturnEmptyList() throws Exception {
        // Given
        Long userId = 1L;

        when(itemService.getUserItems(eq(userId)))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/items")
                        .header(USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void searchItems_shouldReturnEmptyList() throws Exception {
        // Given
        when(itemService.searchItems(anyString())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}