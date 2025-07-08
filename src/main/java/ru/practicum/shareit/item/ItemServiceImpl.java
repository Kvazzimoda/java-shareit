package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final Map<Long, Item> items = new HashMap<>();
    private Long itemIdCounter = 1L;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User user = userService.getUserModel(userId);
        if (user == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }
        Item item = ItemMapper.toItem(itemDto, userId);
        item.setId(itemIdCounter++);
        items.put(item.getId(), item);
        return ItemMapper.toDto(item);
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemUpdateDto itemUpdateDto) {
        Item item = items.get(itemId);
        if (item == null || !item.getOwnerId().equals(userId)) {
            throw new ForbiddenException("Only owner can update item");
        }
        if (itemUpdateDto.getName() != null) item.setName(itemUpdateDto.getName());
        if (itemUpdateDto.getDescription() != null) item.setDescription(itemUpdateDto.getDescription());
        if (itemUpdateDto.getAvailable() != null) item.setAvailable(itemUpdateDto.getAvailable());
        items.put(itemId, item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto getItem(Long itemId, Long userId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }
        return ItemMapper.toDto(item);
    }

    @Override
    public Item getItemModel(Long itemId, Long userId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }
        return item; // Возвращаем модель напрямую
    }

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(searchText) ||
                        item.getDescription().toLowerCase().contains(searchText))
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}