package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void toDto_shouldMapCorrectly() {
        // Given
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        // When
        ItemDto dto = ItemMapper.toDto(item);

        // Then
        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getAvailable(), dto.getAvailable());
    }

    @Test
    void toItem_shouldMapCorrectly() {
        // Given
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Test Item");
        dto.setDescription("Test Description");
        dto.setAvailable(true);

        User owner = new User();
        ItemRequest request = new ItemRequest();

        // When
        Item item = ItemMapper.toItem(dto, owner, request);

        // Then
        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getDescription(), item.getDescription());
        assertEquals(dto.getAvailable(), item.getAvailable());
        assertEquals(owner, item.getOwner());
        assertEquals(request, item.getRequest());
    }

    @Test
    void toUpdateDto_shouldMapAllFieldsFromItemDto() {
        // Given
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(1L); // Это поле не должно маппиться

        // When
        ItemUpdateDto result = ItemMapper.toUpdateDto(itemDto);

        // Then
        assertNotNull(result);
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.getAvailable());
    }

    @Test
    void toUpdateDto_shouldHandleNullValues() {
        // Given
        ItemDto itemDto = new ItemDto(); // Все поля null

        // When
        ItemUpdateDto result = ItemMapper.toUpdateDto(itemDto);

        // Then
        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getDescription());
        assertNull(result.getAvailable());
    }

    @Test
    void toDtoFromUpdateDto_shouldMapAllFields() {
        // Given
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated Item");
        updateDto.setDescription("Updated Description");
        updateDto.setAvailable(false);

        // When
        ItemDto result = ItemMapper.toDto(updateDto);

        // Then
        assertNotNull(result);
        assertEquals(updateDto.getName(), result.getName());
        assertEquals(updateDto.getDescription(), result.getDescription());
        assertEquals(updateDto.getAvailable(), result.getAvailable());
        // Проверяем, что остальные поля null
        assertNull(result.getId());
        assertNull(result.getRequestId());
        assertNull(result.getComments());
    }

    @Test
    void toDtoFromUpdateDto_shouldHandleNullValues() {
        // Given
        ItemUpdateDto updateDto = new ItemUpdateDto(); // Все поля null

        // When
        ItemDto result = ItemMapper.toDto(updateDto);

        // Then
        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getDescription());
        assertNull(result.getAvailable());
    }
}