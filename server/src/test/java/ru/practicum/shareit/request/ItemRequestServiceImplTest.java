package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private ItemRequestRepository requestRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Test
    void createRequest_shouldSaveNewRequest() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(1L);
        savedRequest.setDescription("Need a drill");
        savedRequest.setRequester(user);
        savedRequest.setCreated(LocalDateTime.now());

        when(userService.getUserModel(userId)).thenReturn(user);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(savedRequest);

        // When
        ItemRequestDto result = requestService.createRequest(userId, requestDto);

        // Then
        assertNotNull(result.getId());
        assertEquals(requestDto.getDescription(), result.getDescription());
        assertEquals(userId, result.getRequesterId());
        assertNotNull(result.getCreated());

        verify(userService).getUserModel(userId);
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void getUserRequests_shouldReturnListOfRequests() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a drill");
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());

        when(userService.getUserModel(userId)).thenReturn(user);
        when(requestRepository.findByRequesterIdOrderByCreatedDesc(userId))
                .thenReturn(Collections.singletonList(request));

        // When
        List<ItemRequestResponseDto> result = requestService.getUserRequests(userId);

        // Then
        assertEquals(1, result.size());
        assertEquals(request.getId(), result.get(0).getId());
        assertEquals(request.getDescription(), result.get(0).getDescription());

        verify(userService).getUserModel(userId);
        verify(requestRepository).findByRequesterIdOrderByCreatedDesc(userId);
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() {
        // Given
        Long userId = 1L;
        User otherUser = new User();
        otherUser.setId(2L);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a saw");
        request.setRequester(otherUser);
        request.setCreated(LocalDateTime.now());

        when(userService.getUserModel(userId)).thenReturn(new User());
        when(requestRepository.findByRequesterIdNotOrderByCreatedDesc(userId))
                .thenReturn(Collections.singletonList(request));

        // When
        List<ItemRequestResponseDto> result = requestService.getAllRequests(userId);

        // Then
        assertEquals(1, result.size());
        assertEquals(request.getId(), result.get(0).getId());
        assertEquals(request.getDescription(), result.get(0).getDescription());

        verify(userService).getUserModel(userId);
        verify(requestRepository).findByRequesterIdNotOrderByCreatedDesc(userId);
    }

    @Test
    void getRequest_shouldReturnRequestWhenExists() {
        // Given
        Long userId = 1L;
        Long requestId = 1L;
        User user = new User();
        user.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Need a hammer");
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());

        when(userService.getUserModel(userId)).thenReturn(user);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // When
        ItemRequestResponseDto result = requestService.getRequest(requestId, userId);

        // Then
        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals(request.getDescription(), result.getDescription());

        verify(userService).getUserModel(userId);
        verify(requestRepository).findById(requestId);
    }

    @Test
    void getRequest_shouldThrowExceptionWhenNotFound() {
        // Given
        Long userId = 1L;
        Long requestId = 99L;

        when(userService.getUserModel(userId)).thenReturn(new User());
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> requestService.getRequest(requestId, userId));

        verify(userService).getUserModel(userId);
        verify(requestRepository).findById(requestId);
    }

    @Test
    void createRequest_shouldCorrectlyMapEntityToDto() {
        // Подготовка данных
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        ItemRequestDto inputDto = new ItemRequestDto();
        inputDto.setDescription("Need a drill");

        // Создаём объект, который должен вернуть репозиторий
        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(1L);
        savedRequest.setDescription("Need a drill");
        savedRequest.setRequester(user);
        savedRequest.setCreated(LocalDateTime.now());

        // Мокаем зависимости
        when(userService.getUserModel(userId)).thenReturn(user);
        when(requestRepository.save(any())).thenReturn(savedRequest);

        // Вызываем публичный метод
        ItemRequestDto result = requestService.createRequest(userId, inputDto);

        // Проверяем, что маппинг прошёл корректно
        assertEquals(savedRequest.getId(), result.getId());
        assertEquals(savedRequest.getDescription(), result.getDescription());
        assertEquals(userId, result.getRequesterId());
        assertEquals(savedRequest.getCreated(), result.getCreated());
    }

    @Test
    void getRequest_shouldCorrectlyMapEntityToResponseDto() {
        // Подготовка данных
        Long userId = 1L;
        Long requestId = 1L;
        User user = new User();
        user.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Need a hammer");
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());

        // Мокаем зависимости
        when(userService.getUserModel(userId)).thenReturn(user);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // Вызываем публичный метод
        ItemRequestResponseDto result = requestService.getRequest(requestId, userId);

        // Проверяем маппинг
        assertEquals(request.getId(), result.getId());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(userId, result.getRequesterId());
        assertEquals(request.getCreated(), result.getCreated());
        assertTrue(result.getItems().isEmpty()); // Проверяем и items
    }

    @Test
    void getRequest_shouldMapItemsCorrectly() {
        // Given
        Long userId = 1L;
        Long requestId = 1L;

        // Создаем пользователя
        User requester = new User();
        requester.setId(userId);

        // Создаем владельца вещи
        User owner = new User();
        owner.setId(2L);

        // Создаем вещь
        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setOwner(owner);

        // Создаем запрос с привязанной вещью
        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Нужна дрель");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());
        request.setItems(List.of(item)); // Добавляем вещь в запрос

        // Мокаем зависимости
        when(userService.getUserModel(userId)).thenReturn(requester);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // When
        ItemRequestResponseDto result = requestService.getRequest(requestId, userId);

        // Then
        assertEquals(1, result.getItems().size()); // Проверяем, что вещь добавилась

        ItemRequestResponseDto.ItemResponse itemResponse = result.getItems().get(0);
        assertEquals(item.getId(), itemResponse.getItemId());
        assertEquals(item.getName(), itemResponse.getName());
        assertEquals(item.getOwner().getId(), itemResponse.getOwnerId());
    }
}