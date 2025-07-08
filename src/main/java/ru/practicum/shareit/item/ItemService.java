package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);
    ItemDto updateItem(Long userId, Long itemId, ItemUpdateDto itemUpdateDto);
    ItemDto getItem(Long itemId, Long userId);
    Item getItemModel(Long itemId, Long userId);
    List<ItemDto> getUserItems(Long userId);
    List<ItemDto> searchItems(String text);
}