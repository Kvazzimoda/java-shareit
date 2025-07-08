package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public static ItemDto toDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        return dto;
    }

    public static Item toItem(ItemDto dto, Long ownerId) {
        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setOwnerId(ownerId);
        return item;
    }

    public static ItemUpdateDto toUpdateDto(ItemDto dto) {
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName(dto.getName());
        updateDto.setDescription(dto.getDescription());
        updateDto.setAvailable(dto.getAvailable());
        return updateDto;
    }

    public static ItemDto toDto(ItemUpdateDto updateDto) {
        ItemDto dto = new ItemDto();
        dto.setName(updateDto.getName());
        dto.setDescription(updateDto.getDescription());
        dto.setAvailable(updateDto.getAvailable());
        return dto;
    }
}