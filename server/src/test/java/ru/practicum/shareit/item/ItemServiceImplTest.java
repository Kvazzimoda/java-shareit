package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_shouldCreateItemWithRequest() {
        // Given
        Long userId = 1L;
        Long requestId = 1L;
        User user = new User();
        user.setId(userId);

        ItemDto inputDto = new ItemDto();
        inputDto.setRequestId(requestId);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);

        Item expectedItem = new Item();
        expectedItem.setRequest(request);

        when(userService.getUserModel(userId)).thenReturn(user);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenReturn(expectedItem);

        // When
        ItemDto result = itemService.createItem(userId, inputDto);

        // Then
        assertNotNull(result);
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void createItem_shouldThrowExceptionWhenUserNotFound() {
        // Given
        Long userId = 99L;
        ItemDto itemDto = new ItemDto();

        when(userService.getUserModel(userId)).thenReturn(null);

        // When & Then
        assertThrows(NotFoundException.class, () -> itemService.createItem(userId, itemDto));
        verify(userService).getUserModel(userId);
        verifyNoInteractions(itemRequestRepository, itemRepository);
    }

    @Test
    void createItem_shouldThrowExceptionWhenRequestNotFound() {
        // Given
        Long userId = 1L;
        Long requestId = 99L;
        User user = new User();
        user.setId(userId);

        ItemDto itemDto = new ItemDto();
        itemDto.setRequestId(requestId);

        when(userService.getUserModel(userId)).thenReturn(user);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> itemService.createItem(userId, itemDto));
        verify(itemRequestRepository).findById(requestId);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void updateItem_shouldUpdateFields() {
        // Given
        Long userId = 1L;
        Long itemId = 1L;
        User owner = new User();
        owner.setId(userId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setOwner(owner);

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated Name");
        updateDto.setDescription("Updated Description");
        updateDto.setAvailable(false);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(existingItem);

        // When
        ItemDto result = itemService.updateItem(userId, itemId, updateDto);

        // Then
        assertEquals("Updated Name", existingItem.getName());
        assertEquals("Updated Description", existingItem.getDescription());
        assertFalse(existingItem.getAvailable());
    }

    @Test
    void updateItem_shouldThrowExceptionWhenItemNotFound() {
        // Given
        Long userId = 1L;
        Long itemId = 99L;
        ItemUpdateDto updateDto = new ItemUpdateDto();

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> itemService.updateItem(userId, itemId, updateDto));
        verify(itemRepository).findById(itemId);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void updateItem_shouldThrowExceptionWhenNotOwner() {
        // Given
        Long ownerId = 1L;
        Long otherUserId = 2L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(ownerId);

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        ItemUpdateDto updateDto = new ItemUpdateDto();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // When & Then
        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(otherUserId, itemId, updateDto));
        verify(itemRepository).findById(itemId);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() {
        // Given
        Long userId = 1L;
        Long itemId = 1L;
        User owner = new User();
        owner.setId(userId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setOwner(owner);
        existingItem.setName("Old Name");
        existingItem.setDescription("Old Desc");
        existingItem.setAvailable(true);

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("New Name");
        updateDto.setDescription("New Desc");
        updateDto.setAvailable(false);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenReturn(existingItem);

        // When
        ItemDto result = itemService.updateItem(userId, itemId, updateDto);

        // Then
        assertNotNull(result, "Result should not be null"); // Проверяем, что результат не null
        assertEquals(itemId, result.getId(), "Item ID should match");
        assertEquals("New Name", result.getName(), "Item name should be updated");
        assertEquals("New Desc", result.getDescription(), "Item description should be updated");
        assertFalse(result.getAvailable(), "Item availability should be updated");
        assertEquals("New Name", existingItem.getName(), "Existing item name should be updated");
        assertEquals("New Desc", existingItem.getDescription(), "Existing item description should be updated");
        assertFalse(existingItem.getAvailable(), "Existing item availability should be updated");
    }

    @Test
    void getItem_shouldReturnWithBookingsForOwner() {
        // Given
        Long ownerId = 1L;
        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        User owner = new User();
        owner.setId(ownerId);
        item.setOwner(owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(itemId)).thenReturn(Collections.emptyList());
        when(commentRepository.findByItemId(itemId)).thenReturn(Collections.emptyList());

        // When
        ItemDto result = itemService.getItem(itemId, ownerId);

        // Then
        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void getItemModel_shouldThrowExceptionWhenItemNotFound() {
        // Given
        Long itemId = 99L;
        Long userId = 1L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> itemService.getItemModel(itemId, userId));
        verify(itemRepository).findById(itemId);
    }

    @Test
    void getUserItems_shouldReturnItemsWithBookingsAndComments() {
        // Given
        Long userId = 1L;
        Item item = new Item();
        item.setId(1L);
        User owner = new User();
        owner.setId(userId);
        item.setOwner(owner);

        when(itemRepository.findByOwnerId(userId)).thenReturn(List.of(item));
        when(bookingRepository.findByItemId(anyLong())).thenReturn(Collections.emptyList());
        when(commentRepository.findByItemId(anyLong())).thenReturn(Collections.emptyList());

        // When
        List<ItemDto> result = itemService.getUserItems(userId);

        // Then
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
        assertNull(result.get(0).getLastBooking());
        assertNull(result.get(0).getNextBooking());
        assertTrue(result.get(0).getComments().isEmpty());
    }

    @Test
    void searchItems_shouldReturnItemsForValidSearch() {
        // Given
        String searchText = "drill";
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setAvailable(true);

        when(itemRepository.searchAvailableByNameOrDescription(anyString()))
                .thenReturn(List.of(item));

        // When
        List<ItemDto> result = itemService.searchItems(searchText);

        // Then
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
    }

    @Test
    void searchItems_shouldReturnEmptyListForBlankText() {
        // When
        List<ItemDto> result = itemService.searchItems(" ");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_shouldReturnEmptyListForNullText() {
        // When
        List<ItemDto> result = itemService.searchItems(null);

        // Then
        assertTrue(result.isEmpty());
    }
}