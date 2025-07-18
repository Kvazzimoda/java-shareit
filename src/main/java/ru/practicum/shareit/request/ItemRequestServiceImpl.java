package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserService userService;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto) {
        User user = userService.getUserModel(userId);
        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());
        request = requestRepository.save(request);
        return mapToDto(request);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        userService.getUserModel(userId); // Проверка существования пользователя
        return requestRepository.findByRequesterId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        userService.getUserModel(userId); // Проверка существования пользователя
        return requestRepository.findAll().stream()
                .filter(r -> !r.getRequester().getId().equals(userId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequest(Long requestId, Long userId) {
        userService.getUserModel(userId); // Проверка существования пользователя
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        return mapToDto(request);
    }

    private ItemRequestDto mapToDto(ItemRequest request) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setRequesterId(request.getRequester().getId());
        dto.setCreated(request.getCreated());
        return dto;
    }
}