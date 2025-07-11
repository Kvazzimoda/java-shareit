package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserService userService;
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private Long requestIdCounter = 1L;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto) {
        UserDto userDto = userService.getUser(userId); // Используем существующий метод
        if (userDto == null) {
            throw new IllegalArgumentException("User not found");
        }
        User user = UserMapper.toUser(userDto); // Преобразуем DTO в модель
        ItemRequest request = new ItemRequest();
        request.setId(requestIdCounter++);
        request.setDescription(requestDto.getDescription());
        request.setRequesterId(userId);
        request.setCreated(LocalDateTime.now());
        requests.put(request.getId(), request);
        return mapToDto(request);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        return requests.values().stream()
                .filter(r -> r.getRequesterId().equals(userId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        return requests.values().stream()
                .filter(r -> !r.getRequesterId().equals(userId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequest(Long requestId, Long userId) {
        ItemRequest request = requests.get(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Request not found");
        }
        return mapToDto(request);
    }

    private ItemRequestDto mapToDto(ItemRequest request) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        return dto;
    }
}