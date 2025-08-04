package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.stream.Collectors;

@UtilityClass
public class ItemMapper {
    public ItemDto toDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());

        if (item.getRequest() != null) {
            dto.setRequestId(item.getRequest().getId());
        }

        dto.setComments(item.getComments().stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList()));

        return dto;
    }

    public Item toItem(ItemDto dto, User owner, ItemRequest request) {
        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }

    public ItemUpdateDto toUpdateDto(ItemDto dto) {
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName(dto.getName());
        updateDto.setDescription(dto.getDescription());
        updateDto.setAvailable(dto.getAvailable());
        return updateDto;
    }

    public ItemDto toDto(ItemUpdateDto updateDto) {
        ItemDto dto = new ItemDto();
        dto.setName(updateDto.getName());
        dto.setDescription(updateDto.getDescription());
        dto.setAvailable(updateDto.getAvailable());
        return dto;
    }
}
